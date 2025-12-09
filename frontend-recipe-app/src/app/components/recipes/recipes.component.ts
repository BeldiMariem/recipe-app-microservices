import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { RecipeService } from '../../services/recipe.service';
import { AuthService } from '../../services/auth.service';
import { Recipe, RecipeIngredient } from '../../models/recipe.model';

@Component({
  selector: 'app-recipes',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './recipes.component.html',
  styleUrls: ['./recipes.component.scss']
})
export class RecipesComponent implements OnInit {
  private recipeService = inject(RecipeService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
    private cdr = inject(ChangeDetectorRef);

  recipes: Recipe[] = [];
  filteredRecipes: Recipe[] = [];
  isLoggedIn = false;

  searchQuery = '';
  selectedDifficulty = 'all';
  sortBy = 'newest';
  viewMode: 'grid' | 'list' = 'grid';

  currentPage = 1;
  pageSize = 12;
  totalPages = 1;

  showCreateModal = false;
  isCreating = false;
  recipeForm: FormGroup;
  tags: string[] = [];
  tagInput = '';

  constructor() {
    this.recipeForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      imageUrl: [''],
      preparationTime: [30, [Validators.required, Validators.min(1)]],
      servings: [4, [Validators.required, Validators.min(1)]],
      difficulty: ['MEDIUM', Validators.required],
      visibility: ['PUBLIC', Validators.required],
      ingredients: this.fb.array([this.createIngredient()]),
      instructions: this.fb.array([this.createInstruction()])
    });
  }

  ngOnInit() {
    this.checkAuth();
    this.loadRecipes();
  }

  get ingredientsArray(): FormArray {
    return this.recipeForm.get('ingredients') as FormArray;
  }

  get instructionsArray(): FormArray {
    return this.recipeForm.get('instructions') as FormArray;
  }

  private checkAuth(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
  }

  loadRecipes(): void {
                this.cdr.detectChanges(); 
    
    if (this.isLoggedIn) {
      this.recipeService.getAllRecipes().subscribe({
        next: (recipes) => {
          this.recipes = recipes;
          this.filterRecipes();
                this.cdr.detectChanges(); 
        },
        error: (error) => {
          console.error('Error loading recipes:', error);
                this.cdr.detectChanges(); 
        }
      });
    } else {
      this.recipeService.getPublicRecipes().subscribe({
        next: (recipes) => {
          this.recipes = recipes;
          this.filterRecipes();
                this.cdr.detectChanges(); 
        },
        error: (error) => {
          console.error('Error loading public recipes:', error);
                this.cdr.detectChanges(); 
        }
      });
    }
  }

  filterRecipes(): void {
    let filtered = [...this.recipes];

    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(recipe =>
        recipe.title.toLowerCase().includes(query) ||
        recipe.description.toLowerCase().includes(query)
      );
    }

    if (this.selectedDifficulty !== 'all') {
      filtered = filtered.filter(recipe =>
        recipe.difficulty === this.selectedDifficulty
      );
    }
    this.totalPages = Math.ceil(filtered.length / this.pageSize);
    this.filteredRecipes = filtered;
    this.currentPage = 1;
  }

  get totalRecipes(): number {
    return this.recipes.length;
  }

  getDifficultyCount(difficulty: string): number {
    return this.recipes.filter(recipe => recipe.difficulty === difficulty).length;
  }

  getPopularRecipesCount(): number {
    return this.recipes.filter(recipe => recipe.rating >= 4.5).length;
  }

  setViewMode(mode: 'grid' | 'list'): void {
    this.viewMode = mode;
  }

  getPageNumbers(): number[] {
    const pages = [];
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
  }

  viewRecipeDetails(recipeId: number): void {
    this.router.navigate(['/recipes', recipeId]);
  }

  saveRecipe(event: Event, recipe: Recipe): void {
    event.stopPropagation();
    console.log('Saving recipe:', recipe.title);
  }

  showMyRecipes(): void {
    if (this.isLoggedIn) {
      this.recipeService.getMyRecipes().subscribe({
        next: (recipes) => {
          this.recipes = recipes;
          this.filterRecipes();
                          this.cdr.detectChanges(); 

        },
        error: (error) => {
          console.error('Error loading my recipes:', error);
                          this.cdr.detectChanges(); 

        }
      });
    }
  }

  getDifficultyClass(difficulty: string): string {
    if (!difficulty) return 'difficulty-unknown';
    return 'difficulty-' + difficulty.toLowerCase();
  }

  getDifficultyText(difficulty: string): string {
    if (!difficulty) return 'Unknown';
    return difficulty.charAt(0).toUpperCase() + difficulty.slice(1).toLowerCase();
  }

  getVisibilityIcon(visibility: string): string {
    return visibility === 'PUBLIC' ? 'bi-globe' : 'bi-lock';
  }


  handleImageError(event: Event, recipeId: number): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.style.display = 'none';
  }

  openCreateRecipeModal(): void {
    if (!this.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.recipeForm.reset({
      preparationTime: 30,
      servings: 4,
      difficulty: 'MEDIUM',
      visibility: 'PUBLIC',
      ingredients: [this.createIngredient()],
      instructions: [this.createInstruction()]
    });
    this.tags = [];
    this.tagInput = '';
  }

  createIngredient(): FormGroup {
    return this.fb.group({
      name: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.1)]],
      unit: ['', Validators.required]
    });
  }

  createInstruction(): FormGroup {
    return this.fb.group({
      step: ['', Validators.required]
    });
  }

  addIngredient(): void {
    this.ingredientsArray.push(this.createIngredient());
  }

  removeIngredient(index: number): void {
    this.ingredientsArray.removeAt(index);
  }

  addInstruction(): void {
    this.instructionsArray.push(this.createInstruction());
  }

  removeInstruction(index: number): void {
    if (this.instructionsArray.length > 1) {
      this.instructionsArray.removeAt(index);
    }
  }

  addTag(): void {
    if (this.tagInput.trim()) {
      this.tags.push(this.tagInput.trim());
      this.tagInput = '';
    }
  }

  removeTag(index: number): void {
    this.tags.splice(index, 1);
  }

  createRecipe(): void {
    if (this.recipeForm.invalid) return;

    this.isCreating = true;
    const formValue = this.recipeForm.value;
    
    const recipeData = {
      title: formValue.title,
      description: formValue.description,
      imageUrl: formValue.imageUrl || null,
      preparationTime: formValue.preparationTime,
      servings: formValue.servings,
      difficulty: formValue.difficulty,
      visibility: formValue.visibility,
      ingredients: formValue.ingredients,
      instructions: formValue.instructions.map((inst: any) => inst.step),
    };

    this.recipeService.createRecipe(recipeData).subscribe({
      next: (recipe) => {
        console.log('Recipe created successfully:', recipe);
        this.isCreating = false;
        this.closeCreateModal();
        this.loadRecipes(); 
      },
      error: (error) => {
        console.error('Error creating recipe:', error);
        this.isCreating = false;
      }
    });
  }
}
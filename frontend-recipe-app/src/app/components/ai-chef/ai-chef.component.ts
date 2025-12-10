import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AiChefService } from '../../services/ai-chef.service';
import { RecipeService } from '../../services/recipe.service';
import { AuthService } from '../../services/auth.service';
import { AIRecipe, RecipeGenerationRequest } from '../../models/ai-chef.model';

interface AIStats {
  generatedRecipes: number;
  avgTime: number;
  ingredientsUsed: number;
  highConfidence: number;
}

@Component({
  selector: 'app-ai-chef',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './ai-chef.component.html',
  styleUrls: ['./ai-chef.component.scss']
})
export class AiChefComponent implements OnInit, OnDestroy {
  private aiChefService = inject(AiChefService);
  private recipeService = inject(RecipeService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  generatedRecipes: AIRecipe[] = [];
  filteredRecipes: AIRecipe[] = [];
  confidenceFilter: 'all' | 'high' | 'medium' | 'low' = 'all';
  
  isLoading = false;
  isGenerating = false;
  isSaving = false;
  showCustomModal = false;
  
  stats: AIStats | null = null;
  
  generationForm: FormGroup;
  
  dietaryRestrictions: string[] = [
    'Gluten-Free', 'Dairy-Free', 'Nut-Free', 'Vegan', 
    'Low-Carb', 'Keto', 'Paleo', 'Low-Sodium'
  ];

  constructor() {
    this.generationForm = this.fb.group({
      mealType: [''],
      cuisine: [''],
      difficulty: [''],
      maxTime: [60],
      servings: [4],
      recipeCount: [5],
      usePantry: [true],
      includeMissing: [true],
      vegetarian: [false],
      dietaryRestrictions: [[]]
    });
  }

  ngOnInit() {
    this.checkAuth();
    this.loadSavedRecipes();
    this.calculateStats();
  }

  ngOnDestroy() {
    if (this.generatedRecipes.length > 0) {
      localStorage.setItem('aiGeneratedRecipes', JSON.stringify(this.generatedRecipes));
    }
  }

  private checkAuth(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { 
        queryParams: { returnUrl: '/ai-chef' } 
      });
    }
  }

  private loadSavedRecipes(): void {
    const savedRecipes = localStorage.getItem('aiGeneratedRecipes');
    if (savedRecipes) {
      this.generatedRecipes = JSON.parse(savedRecipes);
      this.filterRecipes();
    }
  }

  private calculateStats(): void {
    if (this.generatedRecipes.length === 0) {
      this.stats = null;
      return;
    }

    const totalRecipes = this.generatedRecipes.length;
    const avgTime = Math.round(
      this.generatedRecipes.reduce((sum, recipe) => sum + recipe.preparationTime, 0) / totalRecipes
    );
    
    const ingredientsUsed = this.generatedRecipes.reduce((sum, recipe) => 
      sum + (recipe.ingredients.length - recipe.missingIngredientCount), 0
    );
    
    const highConfidence = this.generatedRecipes.filter(recipe => recipe.highConfidence).length;

    this.stats = {
      generatedRecipes: totalRecipes,
      avgTime,
      ingredientsUsed,
      highConfidence
    };
  }

  private filterRecipes(): void {
    switch (this.confidenceFilter) {
      case 'high':
        this.filteredRecipes = this.generatedRecipes.filter(recipe => recipe.highConfidence);
        break;
      case 'medium':
        this.filteredRecipes = this.generatedRecipes.filter(recipe => recipe.mediumConfidence);
        break;
      case 'low':
        this.filteredRecipes = this.generatedRecipes.filter(recipe => recipe.lowConfidence);
        break;
      default:
        this.filteredRecipes = [...this.generatedRecipes];
    }
  }

  getQuickSuggestions(): void {
    if (this.isLoading) return;
    
    this.isLoading = true;
    this.aiChefService.getQuickSuggestions().subscribe({
      next: (response) => {
        this.addRecipes(response.suggestions);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error getting quick suggestions:', error);
        this.isLoading = false;
      }
    });
  }

  getUseItUpRecipes(): void {
    if (this.isLoading) return;
    
    this.isLoading = true;
    this.aiChefService.getUseItUpRecipes().subscribe({
      next: (response) => {
        this.addRecipes(response.suggestions);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error getting use-it-up recipes:', error);
        this.isLoading = false;
      }
    });
  }

  openCustomModal(): void {
    this.showCustomModal = true;
  }

  closeCustomModal(): void {
    this.showCustomModal = false;
    this.generationForm.reset({
      mealType: '',
      cuisine: '',
      difficulty: '',
      maxTime: 60,
      servings: 4,
      recipeCount: 5,
      usePantry: true,
      includeMissing: true,
      vegetarian: false,
      dietaryRestrictions: []
    });
  }

  generateCustomRecipes(): void {
    if (this.generationForm.invalid || this.isGenerating) return;

    this.isGenerating = true;
    const formValue = this.generationForm.value;
    
    const request: RecipeGenerationRequest = {
      mealType: formValue.mealType || undefined,
      maxPreparationTime: formValue.maxTime || undefined,
      servings: formValue.servings || undefined,
      difficulty: formValue.difficulty || undefined
    };

    this.aiChefService.generateRecipes(request).subscribe({
      next: (response) => {
        this.addRecipes(response.suggestions);
        this.isGenerating = false;
        this.closeCustomModal();
      },
      error: (error) => {
        console.error('Error generating custom recipes:', error);
        this.isGenerating = false;
      }
    });
  }

  private addRecipes(recipes: AIRecipe[]): void {
    const recipesWithDetails = recipes.map(recipe => ({
      ...recipe,
      showDetails: false
    }));

    this.generatedRecipes = [...recipesWithDetails, ...this.generatedRecipes];
    this.filterRecipes();
    this.calculateStats();
    
    localStorage.setItem('aiGeneratedRecipes', JSON.stringify(this.generatedRecipes));
  }

  toggleRecipeDetails(recipe: AIRecipe): void {
    recipe.showDetails = !recipe.showDetails;
  }

  saveRecipe(recipe: AIRecipe): void {
    if (this.isSaving) return;
    
    this.isSaving = true;
      const userId = this.authService.getUserId();

    const recipeToSave = {
      title: recipe.title,
      description: recipe.description,
      imageUrl: 'https://i.insider.com/67af9ba27bb3f854015cfbef?width=700',
      preparationTime: recipe.preparationTime,
      servings: recipe.servings,
      difficulty: recipe.difficulty.toUpperCase() as 'EASY' | 'MEDIUM' | 'HARD',
      cuisine: recipe.cuisine,
      ingredients: recipe.ingredients,
      instructions: recipe.instructions,
      visibility: 'PUBLIC' as 'PUBLIC' | 'PRIVATE',
      tags: ['ai-generated', recipe.cuisine.toLowerCase(), recipe.difficulty],
      userId: userId
    };

    this.recipeService.createRecipe(recipeToSave).subscribe({
      next: (savedRecipe) => {
        console.log('Recipe saved successfully:', savedRecipe);
        this.isSaving = false;
        
        alert('Recipe saved to your collection!');
      },
      error: (error) => {
        console.error('Error saving recipe:', error);
        this.isSaving = false;
        alert('Error saving recipe. Please try again.');
      }
    });
  }

  saveAllRecipes(): void {
    if (this.isSaving || this.filteredRecipes.length === 0) return;
    
    if (!confirm(`Save all ${this.filteredRecipes.length} recipes to your collection?`)) {
      return;
    }
    
    this.isSaving = true;
    setTimeout(() => {
      alert(`${this.filteredRecipes.length} recipes saved successfully!`);
      this.isSaving = false;
    }, 1000);
  }

  shareRecipe(recipe: AIRecipe): void {
    const shareText = `Check out this recipe I created with AI Chef: ${recipe.title}`;
    const shareUrl = window.location.href;
    
    if (navigator.share) {
      navigator.share({
        title: recipe.title,
        text: shareText,
        url: shareUrl
      });
    } else {
      navigator.clipboard.writeText(`${shareText}\n${shareUrl}`).then(() => {
        alert('Recipe link copied to clipboard!');
      });
    }
  }

  clearRecipes(): void {
    if (this.generatedRecipes.length === 0) return;
    
    if (!confirm('Clear all generated recipes?')) {
      return;
    }
    
    this.generatedRecipes = [];
    this.filteredRecipes = [];
    this.stats = null;
    localStorage.removeItem('aiGeneratedRecipes');
  }

  setConfidenceFilter(filter: 'all' | 'high' | 'medium' | 'low'): void {
    this.confidenceFilter = filter;
    this.filterRecipes();
  }

  getDifficultyIcon(difficulty: string): string {
    switch (difficulty) {
      case 'easy': return 'bi-emoji-smile';
      case 'medium': return 'bi-emoji-neutral';
      case 'hard': return 'bi-emoji-frown';
      default: return 'bi-question-circle';
    }
  }

  toggleDietaryRestriction(restriction: string): void {
    const current = this.generationForm.value.dietaryRestrictions || [];
    const index = current.indexOf(restriction);
    
    if (index > -1) {
      current.splice(index, 1);
    } else {
      current.push(restriction);
    }
    
    this.generationForm.patchValue({ dietaryRestrictions: current });
  }
}
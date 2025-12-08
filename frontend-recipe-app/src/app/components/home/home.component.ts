import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { RecipeService } from '../../services/recipe.service';
import { AuthService } from '../../services/auth.service';
import { Recipe } from '../../models/recipe.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  private recipeService = inject(RecipeService);
  private authService = inject(AuthService);
  private router = inject(Router);
private cdr = inject(ChangeDetectorRef);

  publicRecipes: Recipe[] = [];
  isLoading = true;
  isLoggedIn = false;

  imageLoadingStates: { [key: number]: boolean } = {};
  ngOnInit() {
    this.checkAuthAndLoadRecipes();
  }


  private checkAuthAndLoadRecipes(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
    
    if (this.isLoggedIn) {
      this.router.navigate(['/dashboard']);
      return;
    }
    
    this.loadPublicRecipes();
  }




    loadPublicRecipes(): void {
    this.isLoading = true;
    
    this.recipeService.getPublicRecipes().subscribe({
      next: (recipes) => {
        this.publicRecipes = recipes || [];
        
        // Initialize image loading states
        this.publicRecipes.forEach((recipe, index) => {
          this.imageLoadingStates[index] = !!recipe.imageUrl;
        });
        
        this.isLoading = false;
        this.cdr.detectChanges(); // Force change detection
      },
      error: (error) => {
        console.error('Error loading public recipes:', error);
        this.isLoading = false;
        this.publicRecipes = [];
      }
    });
  }

  viewRecipeDetails(recipeId: number): void {
    this.router.navigate(['/recipes', recipeId]);
  }

  getIngredientPreview(ingredients: any[]): string {
    if (!ingredients || ingredients.length === 0) {
      return 'No ingredients listed';
    }
    
    const ingredientNames = ingredients.slice(0, 3).map(ing => ing.name);
    return ingredientNames.join(', ') + (ingredients.length > 3 ? ', ...' : '');
  }

  getDifficultyClass(difficulty: string): string {
    if (!difficulty) return 'difficulty-unknown';
    return 'difficulty-' + difficulty.toLowerCase();
  }

  getDifficultyText(difficulty: string): string {
    if (!difficulty) return 'Unknown';
    return difficulty.charAt(0).toUpperCase() + difficulty.slice(1).toLowerCase();
  }

  handleImageError(event: Event, index: number): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.style.display = 'none';
    this.imageLoadingStates[index] = false;
    this.cdr.detectChanges();
  }

  handleImageLoad(index: number): void {
    this.imageLoadingStates[index] = false;
    this.cdr.detectChanges();
  }



  
}
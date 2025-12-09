import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { RecipeService } from '../../services/recipe.service';
import { Recipe, RecipeIngredient } from '../../models/recipe.model';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-recipe-details',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule],
    templateUrl: './recipe-details.component.html',
    styleUrl: './recipe-details.component.scss'
})
export class RecipeDetailsComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private recipeService = inject(RecipeService);
    private cdr = inject(ChangeDetectorRef);
    recipe: Recipe | null = null;
    relatedRecipes: Recipe[] = [];
    error: string | null = null;
    currentUser = 'You';

    ngOnInit() {
        this.route.params.subscribe(params => {
            const recipeId = +params['id'];
            this.loadRecipe(recipeId);
        });
    }


    loadRecipe(id: number): void {
        this.error = null;

        this.recipeService.getRecipeById(id).subscribe({
            next: (recipe) => {
                this.recipe = recipe;
                this.loadRelatedRecipes();
                this.cdr.detectChanges(); // Add this line
            },
            error: (error) => {
                console.error('Error loading recipe:', error);
                this.error = 'Recipe not found';
                this.cdr.detectChanges(); // Add this line
            }
        });
    }

    loadRelatedRecipes(): void {
        this.recipeService.getPublicRecipes().subscribe({
            next: (recipes) => {
                this.relatedRecipes = recipes
                    .filter(r => r.id !== this.recipe?.id)
                    .sort(() => Math.random() - 0.5)
                    .slice(0, 3);
                this.cdr.detectChanges(); // Add this line
            },
            error: (error) => {
                console.error('Error loading related recipes:', error);
                this.cdr.detectChanges(); // Add this line
            }
        });
    }

    get totalTime(): number {
        if (!this.recipe) return 0;
        return this.recipe.preparationTime;
    }

    getDifficultyClass(difficulty: string): string {
        if (!difficulty) return 'difficulty-unknown';
        return 'difficulty-' + difficulty.toLowerCase();
    }

    getDifficultyText(difficulty: string): string {
        if (!difficulty) return 'Unknown';
        return difficulty.charAt(0).toUpperCase() + difficulty.slice(1).toLowerCase();
    }

    getFormattedInstructions(): { stepNumber: number, description: string }[] {
        if (!this.recipe) return [];

        return this.recipe.instructions.map((instruction, index) => ({
            stepNumber: index + 1,
            description: instruction
        }));
    }

    getCategoryFromDifficulty(): string {
        if (!this.recipe) return 'Recipe';

        switch (this.recipe.difficulty) {
            case 'EASY': return 'Easy Recipe';
            case 'MEDIUM': return 'Intermediate Recipe';
            case 'HARD': return 'Advanced Recipe';
            default: return 'Recipe';
        }
    }

    getFormattedIngredients(): Array<RecipeIngredient & { checked: boolean }> {
        if (!this.recipe) return [];

        return this.recipe.ingredients.map(ingredient => ({
            ...ingredient,
            checked: false
        }));
    }

    toggleIngredient(index: number): void {
        // This will work with the updated HTML template
        const ingredients = this.getFormattedIngredients();
        if (ingredients[index]) {
            ingredients[index].checked = !ingredients[index].checked;
        }
    }

    calculateNutrition(): { calories: number, protein: number, carbs: number, fat: number } {
        // Mock nutrition calculation based on ingredients
        if (!this.recipe || !this.recipe.ingredients?.length) {
            return { calories: 350, protein: 15, carbs: 45, fat: 12 };
        }

        // Simple mock calculation - in real app, you'd have a nutrition API
        const ingredientCount = this.recipe.ingredients?.length;
        const calories = 200 + (ingredientCount * 50);
        const protein = 10 + (ingredientCount * 2);
        const carbs = 30 + (ingredientCount * 3);
        const fat = 8 + (ingredientCount * 1);

        return { calories, protein, carbs, fat };
    }

    startCooking(): void {
        // Implement cooking mode
        console.log('Starting cooking mode for:', this.recipe?.title);
        alert(`Starting to cook: ${this.recipe?.title}! Get your ingredients ready.`);
    }

    shareRecipe(): void {
        if (navigator.share && this.recipe) {
            navigator.share({
                title: this.recipe.title,
                text: `Check out this recipe: ${this.recipe.title} - ${this.recipe.description}`,
                url: window.location.href
            }).catch(err => {
                console.log('Error sharing:', err);
                this.copyToClipboard();
            });
        } else {
            this.copyToClipboard();
        }
    }

    private copyToClipboard(): void {
        navigator.clipboard.writeText(window.location.href)
            .then(() => alert('Recipe link copied to clipboard!'))
            .catch(err => console.error('Failed to copy:', err));
    }

    printRecipe(): void {
        window.print();
    }

    viewRecipe(recipeId: number): void {
        this.router.navigate(['/recipes', recipeId]);
    }

    getRatingStars(): number[] {
        if (!this.recipe) return [];
        const rating = Math.floor(this.recipe.rating || 0);
        return Array(rating).fill(0);
    }

    getEmptyStars(): number[] {
        if (!this.recipe) return [];
        const rating = Math.floor(this.recipe.rating || 0);
        return Array(5 - rating).fill(0);
    }

    getFormattedDate(): string {
        if (!this.recipe) return 'Recently';
        // Mock date - in real app, you'd have createdAt field
        return new Date().toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric'
        });
    }
    clearAllIngredients(): void {
        // Reset all ingredient checkboxes
        const ingredients = this.getFormattedIngredients();
        ingredients.forEach(ing => ing.checked = false);
        // In a real app, you'd update a state variable
        console.log('Cleared all ingredients');
    }
}
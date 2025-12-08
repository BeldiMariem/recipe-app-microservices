import { RecipeIngredient } from "./recipe.model";

export interface AIRecipe {
  title: string;
  description: string;
  ingredients: RecipeIngredient[];
  instructions: string[];
  preparationTime: number;
  servings: number;
  difficulty: 'easy' | 'medium' | 'hard';
  cuisine: string;
  confidenceScore: number;
  missingIngredients: string[];
  missingIngredientCount: number;
  highConfidence: boolean;
  mediumConfidence: boolean;
  lowConfidence: boolean;
}

export interface RecipeGenerationRequest {
  mealType?: string;
  maxPreparationTime?: number;
  servings?: number;
  difficulty?: 'easy' | 'medium' | 'hard';
  userId?: string;
}

export interface RecipeGenerationResponse {
  generationId: string;
  recipeCount: number;
  suggestions: AIRecipe[];
  timestamp: number;
}
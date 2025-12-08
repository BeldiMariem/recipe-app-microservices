export interface RecipeIngredient {
  name: string;
  quantity: number;
  unit: string;
}

export interface Recipe {
  id?: number;
  title: string;
  description: string;
  imageUrl: string | null;
  preparationTime: number;
  servings: number;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  userId: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  ingredients: RecipeIngredient[];
  instructions: string[];
  rating: number;
  ratingCount: number;
}

export interface CreateRecipeRequest {
  title: string;
  description: string;
  imageUrl: string | null;
  preparationTime: number;
  servings: number;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  visibility: 'PUBLIC' | 'PRIVATE';
  ingredients: RecipeIngredient[];
  instructions: string[];
}
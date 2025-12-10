import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recipe, CreateRecipeRequest } from '../models/recipe.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class RecipeService {
  private apiUrl = 'http://localhost:8080';
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders() {
    const token = this.authService.getToken();
    const userId = this.authService.getUserId();
    return {
      'Authorization': `Bearer ${token}`,
      'User-Id': userId
    };
  }

  getRecipeSuggestions(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/api/recipes/suggestions`, {
      headers: this.getHeaders()
    });
  }

  getUseItUpRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/api/recipes/use-it-up`, {
      headers: this.getHeaders()
    });
  }

  getAllRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/api/recipes/all`, {
      headers: this.getHeaders()
    });
  }

  getPublicRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/api/recipes/public`);
  }

  getMyRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/api/recipes/my-recipes`, {
      headers: this.getHeaders()
    });
  }

  getRecipeById(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.apiUrl}/api/recipes/getRecipeById/${id}`, {
      headers: this.getHeaders()
    });
  }

  createRecipe(recipe: CreateRecipeRequest): Observable<Recipe> {
    return this.http.post<Recipe>(`${this.apiUrl}/api/recipes/createRecipe`, recipe, {
      headers: this.getHeaders()
    });
  }

  updateRecipe(id: number, recipe: any): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.apiUrl}/api/recipes/updateRecipe/${id}`, recipe, {
      headers: this.getHeaders()
    });
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/recipes/deleteRecipe/${id}`, {
      headers: this.getHeaders()
    });
  }
}
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recipe, CreateRecipeRequest } from '../models/recipe.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class RecipeService {
  private apiUrl = 'http://localhost:8080/api/recipes';
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
    return this.http.get<Recipe[]>(`${this.apiUrl}/suggestions`, {
      headers: this.getHeaders()
    });
  }

  getUseItUpRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/use-it-up`, {
      headers: this.getHeaders()
    });
  }

  getAllRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/all`, {
      headers: this.getHeaders()
    });
  }

  getPublicRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/public`);
  }

  getMyRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/my-recipes`, {
      headers: this.getHeaders()
    });
  }

  getRecipeById(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.apiUrl}/getRecipeById/${id}`, {
      headers: this.getHeaders()
    });
  }

  createRecipe(recipe: CreateRecipeRequest): Observable<Recipe> {
    return this.http.post<Recipe>(`${this.apiUrl}/createRecipe`, recipe, {
      headers: this.getHeaders()
    });
  }

  updateRecipe(id: number, recipe: any): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.apiUrl}/updateRecipe/${id}`, recipe, {
      headers: this.getHeaders()
    });
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/deleteRecipe/${id}`, {
      headers: this.getHeaders()
    });
  }
}
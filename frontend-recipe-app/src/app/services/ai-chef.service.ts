import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RecipeGenerationRequest, RecipeGenerationResponse } from '../models/ai-chef.model';
import { AuthService } from './auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class AiChefService {
  private apiUrl = environment.apiUrl;

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

  generateRecipes(request: RecipeGenerationRequest): Observable<RecipeGenerationResponse> {
    return this.http.post<RecipeGenerationResponse>(`${this.apiUrl}/api/ai/generate-recipes`, request, {
      headers: this.getHeaders()
    });
  }

  getQuickSuggestions(mealType?: string, maxTime?: number): Observable<RecipeGenerationResponse> {
    let url = `${this.apiUrl}/api/ai/quick-suggestions`;
    const params: string[] = [];
    
    if (mealType) params.push(`mealType=${mealType}`);
    if (maxTime) params.push(`maxTime=${maxTime}`);
    
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }
    
    return this.http.get<RecipeGenerationResponse>(url, {
      headers: this.getHeaders()
    });
  }

  getUseItUpRecipes(): Observable<RecipeGenerationResponse> {
    return this.http.get<RecipeGenerationResponse>(`${this.apiUrl}/api/ai/use-it-up`, {
      headers: this.getHeaders()
    });
  }
}
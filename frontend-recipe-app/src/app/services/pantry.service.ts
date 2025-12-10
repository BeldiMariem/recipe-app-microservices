import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PantryItem, AddPantryItemRequest } from '../models/pantry.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PantryService {
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

  getPantryItems(): Observable<PantryItem[]> {
    return this.http.get<PantryItem[]>(`${this.apiUrl}/api/pantry/items`, {
      headers: this.getHeaders()
    });
  }

  addPantryItem(item: AddPantryItemRequest): Observable<PantryItem> {
    return this.http.post<PantryItem>(`${this.apiUrl}/api/pantry/items/addItem`, item, {
      headers: this.getHeaders()
    });
  }

  updatePantryItem(itemId: number, item: AddPantryItemRequest): Observable<PantryItem> {
    return this.http.put<PantryItem>(`${this.apiUrl}/api/pantry/items/updateItem/${itemId}`, item, {
      headers: this.getHeaders()
    });
  }

  getExpiringItems(): Observable<PantryItem[]> {
    return this.http.get<PantryItem[]>(`${this.apiUrl}/api/pantry/items/expiring`, {
      headers: this.getHeaders()
    });
  }

  getItemById(itemId: number): Observable<PantryItem> {
    return this.http.get<PantryItem>(`${this.apiUrl}/api/pantry/items/getItemById/${itemId}`, {
      headers: this.getHeaders()
    });
  }
  deletePantry(id: number): Observable<void> {
      return this.http.delete<void>(`${this.apiUrl}/api/pantry/deletePantry/${id}`, {
        headers: this.getHeaders()
      });
  }
}
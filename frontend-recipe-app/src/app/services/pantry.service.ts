import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PantryItem, AddPantryItemRequest } from '../models/pantry.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PantryService {
  private apiUrl = 'http://localhost:8080/api/pantry';

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
    return this.http.get<PantryItem[]>(`${this.apiUrl}/items`, {
      headers: this.getHeaders()
    });
  }

  addPantryItem(item: AddPantryItemRequest): Observable<PantryItem> {
    return this.http.post<PantryItem>(`${this.apiUrl}/items/addItem`, item, {
      headers: this.getHeaders()
    });
  }

  updatePantryItem(itemId: number, item: AddPantryItemRequest): Observable<PantryItem> {
    return this.http.put<PantryItem>(`${this.apiUrl}/items/updateItem/${itemId}`, item, {
      headers: this.getHeaders()
    });
  }

  getExpiringItems(): Observable<PantryItem[]> {
    return this.http.get<PantryItem[]>(`${this.apiUrl}/items/expiring`, {
      headers: this.getHeaders()
    });
  }

  getItemById(itemId: number): Observable<PantryItem> {
    return this.http.get<PantryItem>(`${this.apiUrl}/items/getItemById/${itemId}`, {
      headers: this.getHeaders()
    });
  }
}
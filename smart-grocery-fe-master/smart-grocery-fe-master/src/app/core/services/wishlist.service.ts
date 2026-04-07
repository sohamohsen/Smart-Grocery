import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageResponse, FavoriteResponse } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class WishlistService {
  private apiUrl = 'http://localhost:8080/api/wishlist';

  constructor(private http: HttpClient) { }

  toggleFavorite(productId: number): Observable<ApiResponse<FavoriteResponse>> {
    return this.http.post<ApiResponse<FavoriteResponse>>(`${this.apiUrl}/${productId}`, {});
  }

  getWishlist(page: number, size: number): Observable<ApiResponse<PageResponse<FavoriteResponse>>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sortBy', 'addedAt').set('sortDir', 'desc');
    return this.http.get<ApiResponse<PageResponse<FavoriteResponse>>>(this.apiUrl, { params });
  }

  isInWishlist(productId: number): Observable<ApiResponse<boolean>> {
    return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/check/${productId}`);
  }
}

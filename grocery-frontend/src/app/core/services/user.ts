// src/app/core/services/user.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ProductResponse {
  id: number; name: string; description?: string; brand?: string; price: number;
  barcode?: string; imageUrl?: string; isApproved: boolean;
  categoryId: number; categoryName: string; tags?: string[]; createdAt: string;
}
export interface CategoryResponse { id: number; name: string; description?: string; }
export interface FavoriteResponse {
  id: number; productId: number; productName: string; productBrand?: string;
  productPrice: number; productImageUrl?: string; categoryName: string;
  tags?: string[]; addedAt: string;
}
export interface ApiResponse<T> { message: string; data: T; }
export interface PageResponse<T> {
  content: T[]; totalElements: number; totalPages: number; number: number; size: number;
}

@Injectable({ providedIn: 'root' })
export class UserProductService {
  private base = 'http://localhost:8080/api/products';
  private catBase = 'http://localhost:8080/api/categories';
  constructor(private http: HttpClient) {}

  getProducts(p: {
    page?: number; size?: number; search?: string;
    categoryId?: number; sortBy?: string; sortDir?: string;
  }): Observable<ApiResponse<PageResponse<ProductResponse>>> {
    let params = new HttpParams()
      .set('page', p.page ?? 0).set('size', p.size ?? 12)
      .set('sortBy', p.sortBy ?? 'createdAt').set('sortDir', p.sortDir ?? 'desc');
    if (p.search)            params = params.set('search', p.search);
    if (p.categoryId != null) params = params.set('categoryId', p.categoryId);
    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(this.base, { params });
  }

  getProduct(id: number): Observable<ApiResponse<ProductResponse>> {
    return this.http.get<ApiResponse<ProductResponse>>(`${this.base}/${id}`);
  }

  getCategories(): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    const params = new HttpParams().set('page', 0).set('size', 100).set('sortBy', 'name').set('sortDir', 'asc');
    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(this.catBase, { params });
  }
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private base = 'http://localhost:8080/api/wishlist';
  constructor(private http: HttpClient) {}

  toggle(productId: number): Observable<ApiResponse<FavoriteResponse>> {
    return this.http.post<ApiResponse<FavoriteResponse>>(`${this.base}/${productId}`, {});
  }

  isInWishlist(productId: number): Observable<ApiResponse<boolean>> {
    return this.http.get<ApiResponse<boolean>>(`${this.base}/check/${productId}`);
  }

  getWishlist(page = 0, size = 12): Observable<ApiResponse<PageResponse<FavoriteResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sortBy', 'addedAt').set('sortDir', 'desc');
    return this.http.get<ApiResponse<PageResponse<FavoriteResponse>>>(this.base, { params });
  }
}

// src/app/core/services/admin.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// ── Shared interfaces ──────────────────────────────────────────
export interface ApiResponse<T> { message: string; data: T; }
export interface PageResponse<T> {
  content: T[]; totalElements: number; totalPages: number; number: number; size: number;
}

export interface ProductRequest {
  name: string; description?: string; brand?: string; price: number;
  barcode?: string; imageUrl?: string; categoryId: number; tags?: string[];
}
export interface ProductResponse {
  id: number; name: string; description?: string; brand?: string; price: number;
  barcode?: string; imageUrl?: string; isApproved: boolean;
  categoryId: number; categoryName: string; tags?: string[]; createdAt: string;
}
export interface CategoryRequest { name: string; description?: string; }
export interface CategoryResponse { id: number; name: string; description?: string; }

// ── Products ───────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class AdminProductService {
  private base = 'http://localhost:8080/api/admin/products';
  constructor(private http: HttpClient) {}

  getProducts(p: {
    page?: number; size?: number; sortBy?: string; sortDir?: string;
    search?: string; categoryId?: number; barcode?: string;
    isApproved?: boolean; isDeleted?: boolean;
  }): Observable<ApiResponse<PageResponse<ProductResponse>>> {
    let params = new HttpParams()
      .set('page', p.page ?? 0).set('size', p.size ?? 10)
      .set('sortBy', p.sortBy ?? 'createdAt').set('sortDir', p.sortDir ?? 'desc');
    if (p.search)              params = params.set('search', p.search);
    if (p.categoryId != null)  params = params.set('categoryId', p.categoryId);
    if (p.barcode)             params = params.set('barcode', p.barcode);
    if (p.isApproved != null)  params = params.set('isApproved', p.isApproved);
    if (p.isDeleted  != null)  params = params.set('isDeleted',  p.isDeleted);
    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(this.base, { params });
  }

  fetchSuggestion(barcode: string, price: number, categoryId: number): Observable<ApiResponse<ProductRequest>> {
    const params = new HttpParams().set('price', price).set('categoryId', categoryId);
    return this.http.get<ApiResponse<ProductRequest>>(`${this.base}/fetch/${barcode}`, { params });
  }

  addProduct(req: ProductRequest):           Observable<ApiResponse<ProductResponse>>   { return this.http.post<ApiResponse<ProductResponse>>(this.base, req); }
  bulkAddProducts(reqs: ProductRequest[]):   Observable<ApiResponse<ProductResponse[]>> { return this.http.post<ApiResponse<ProductResponse[]>>(`${this.base}/bulk`, reqs); }
  updateProduct(id: number, req: Partial<ProductRequest>): Observable<ApiResponse<ProductResponse>> { return this.http.patch<ApiResponse<ProductResponse>>(`${this.base}/${id}`, req); }
  deleteProduct(id: number):                 Observable<ApiResponse<void>>              { return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`); }
  restoreProduct(id: number):                Observable<ApiResponse<ProductResponse>>   { return this.http.patch<ApiResponse<ProductResponse>>(`${this.base}/restore/${id}`, {}); }
}

// ── Categories ─────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class AdminCategoryService {
  private admin = 'http://localhost:8080/api/admin/categories';
  private user  = 'http://localhost:8080/api/categories';
  constructor(private http: HttpClient) {}

  getCategories(page = 0, size = 200): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(this.user, { params });
  }
  getDeletedCategories(page = 0, size = 10): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(`${this.admin}/deleted`, { params });
  }
  addCategory(req: CategoryRequest):              Observable<ApiResponse<CategoryResponse>> { return this.http.post<ApiResponse<CategoryResponse>>(this.admin, req); }
  updateCategory(id: number, req: CategoryRequest): Observable<ApiResponse<CategoryResponse>> { return this.http.patch<ApiResponse<CategoryResponse>>(`${this.admin}/${id}`, req); }
  deleteCategory(id: number):                     Observable<ApiResponse<void>>             { return this.http.delete<ApiResponse<void>>(`${this.admin}/${id}`); }
  restoreCategory(id: number):                    Observable<ApiResponse<CategoryResponse>> { return this.http.patch<ApiResponse<CategoryResponse>>(`${this.admin}/restore/${id}`, {}); }
}

export class Admin {
}

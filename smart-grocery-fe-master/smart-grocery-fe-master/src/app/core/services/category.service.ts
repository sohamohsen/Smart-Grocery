import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageResponse, CategoryRequest, CategoryResponse } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  getCategories(page: number, size: number, search?: string): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sortBy', 'createdAt').set('sortDir', 'desc');
    if (search) params = params.set('search', search);
    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(`${this.apiUrl}/categories`, { params });
  }

  createCategory(request: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.post<ApiResponse<CategoryResponse>>(`${this.apiUrl}/admin/categories`, request);
  }

  updateCategory(id: number, request: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.patch<ApiResponse<CategoryResponse>>(`${this.apiUrl}/admin/categories/${id}`, request);
  }

  getDeletedCategories(page: number, size: number): Observable<ApiResponse<PageResponse<CategoryResponse>>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', 'createdAt')
      .set('sortDir', 'desc');

    return this.http.get<ApiResponse<PageResponse<CategoryResponse>>>(
      `${this.apiUrl}/super-admin/all/category/deleted`,
      { params }
    );
  }

  deleteCategory(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.apiUrl}/super-admin/deleted/category/${id}`
    );
  }

  restoreCategory(id: number): Observable<ApiResponse<CategoryResponse>> {
    return this.http.patch<ApiResponse<CategoryResponse>>(
      `${this.apiUrl}/super-admin/restore/category/${id}`,
      {}
    );
  }
}

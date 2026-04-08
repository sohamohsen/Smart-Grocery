import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageResponse, ProductRequest, ProductResponse } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  getUserProducts(page: number, size: number, search?: string, categoryId?: number): Observable<ApiResponse<PageResponse<ProductResponse>>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sortBy', 'createdAt').set('sortDir', 'desc');
    if (search) params = params.set('search', search);
    if (categoryId) params = params.set('categoryId', categoryId);
    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(`${this.apiUrl}/products`, { params });
  }

  getAdminProducts(
    page: number, size: number,
    search?: string, categoryId?: number,
    isApproved?: boolean, isDeleted?: boolean
  ): Observable<ApiResponse<PageResponse<ProductResponse>>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sortBy', 'createdAt').set('sortDir', 'desc');
    if (search) params = params.set('search', search);
    if (categoryId) params = params.set('categoryId', categoryId);
    if (isApproved !== undefined) params = params.set('isApproved', isApproved);
    if (isDeleted !== undefined) params = params.set('isDeleted', isDeleted);
    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(`${this.apiUrl}/admin/products`, { params });
  }

  getProductById(id: number): Observable<ApiResponse<ProductResponse>> {
    return this.http.get<ApiResponse<ProductResponse>>(`${this.apiUrl}/products/${id}`);
  }

  addProduct(request: ProductRequest): Observable<ApiResponse<ProductResponse>> {
    return this.http.post<ApiResponse<ProductResponse>>(`${this.apiUrl}/admin/products`, request);
  }

  bulkAddProducts(requests: ProductRequest[]): Observable<ApiResponse<ProductResponse[]>> {
    return this.http.post<ApiResponse<ProductResponse[]>>(`${this.apiUrl}/admin/products/bulk`, requests);
  }

  updateProduct(id: number, request: Partial<ProductRequest>): Observable<ApiResponse<ProductResponse>> {
    return this.http.patch<ApiResponse<ProductResponse>>(`${this.apiUrl}/admin/products/${id}`, request);
  }

  deleteProduct(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/admin/products/${id}`);
  }

  restoreProduct(id: number): Observable<ApiResponse<ProductResponse>> {
    return this.http.patch<ApiResponse<ProductResponse>>(
      `${this.apiUrl}/super-admin/restore/product/${id}`,
      {}
    );
  }

  fetchSuggestion(barcode: string, price: number, categoryId: number): Observable<ApiResponse<ProductRequest>> {
    let params = new HttpParams().set('price', price).set('categoryId', categoryId);
    if (barcode) params = params.set('barcode', barcode);
    return this.http.get<ApiResponse<ProductRequest>>(`${this.apiUrl}/admin/products/fetch`, { params });
  }
}

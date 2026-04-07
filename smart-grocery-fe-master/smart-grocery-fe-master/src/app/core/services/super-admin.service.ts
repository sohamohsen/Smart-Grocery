import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AdminSummaryResponse,
  AuditLogResponse,
  ApiResponse,
  FavoriteResponse,
  PageResponse,
  RegisterRequest
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class SuperAdminService {
  private apiUrl = 'http://localhost:8080/api/super-admin';

  constructor(private http: HttpClient) {}

  getAdmins(
    page: number,
    size: number,
    search?: string
  ): Observable<ApiResponse<PageResponse<AdminSummaryResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', 'createdAt')
      .set('sortDir', 'desc');

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<ApiResponse<PageResponse<AdminSummaryResponse>>>(
      `${this.apiUrl}/admins`,
      { params }
    );
  }

  getUsers(
    page: number,
    size: number,
    search?: string
  ): Observable<ApiResponse<PageResponse<AdminSummaryResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', 'createdAt')
      .set('sortDir', 'desc');

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<ApiResponse<PageResponse<AdminSummaryResponse>>>(
      `${this.apiUrl}/users`,
      { params }
    );
  }

  createAdmin(request: RegisterRequest): Observable<ApiResponse<AdminSummaryResponse>> {
    return this.http.post<ApiResponse<AdminSummaryResponse>>(
      `${this.apiUrl}/add-admin`,
      request
    );
  }

  getAdminActions(
    adminId: number,
    page: number = 0,
    size: number = 8
  ): Observable<ApiResponse<PageResponse<AuditLogResponse>>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', 'createAt')
      .set('sortDir', 'desc');

    return this.http.get<ApiResponse<PageResponse<AuditLogResponse>>>(
      `${this.apiUrl}/admins/${adminId}/actions`,
      { params }
    );
  }

  getUserWishlist(
    userId: number,
    page: number = 0,
    size: number = 8
  ): Observable<ApiResponse<PageResponse<FavoriteResponse>>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', 'addedAt')
      .set('sortDir', 'desc');

    return this.http.get<ApiResponse<PageResponse<FavoriteResponse>>>(
      `${this.apiUrl}/users/${userId}/wishlist`,
      { params }
    );
  }

  updateAccountStatus(
    id: number,
    active: boolean
  ): Observable<ApiResponse<AdminSummaryResponse>> {
    const params = new HttpParams().set('active', String(active));
    return this.http.patch<ApiResponse<AdminSummaryResponse>>(
      `${this.apiUrl}/accounts/${id}/status`,
      null,
      { params }
    );
  }
}

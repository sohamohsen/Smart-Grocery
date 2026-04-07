import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  private readonly adminRoles = ['ADMIN', 'ROLE_ADMIN'];
  private readonly superAdminRoles = ['SUPER_ADMIN', 'ROLE_SUPER_ADMIN'];
  private readonly userRoles = ['USER', 'ROLE_USER'];

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  get currentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  get token(): string | null {
    return this.currentUser?.token || null;
  }

  get role(): string | null {
    return this.currentUser?.role || null;
  }

  isAdminRole(role: string | null = this.role): boolean {
    return !!role && this.adminRoles.includes(role);
  }

  isSuperAdminRole(role: string | null = this.role): boolean {
    return !!role && this.superAdminRoles.includes(role);
  }

  isUserRole(role: string | null = this.role): boolean {
    return !!role && this.userRoles.includes(role);
  }

  canAccessAdminArea(role: string | null = this.role): boolean {
    return this.isAdminRole(role) || this.isSuperAdminRole(role);
  }

  canAccessUserArea(role: string | null = this.role): boolean {
    return this.isAdminRole(role) || this.isUserRole(role);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem('user', JSON.stringify(response));
        this.currentUserSubject.next(response);
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => {
        localStorage.setItem('user', JSON.stringify(response));
        this.currentUserSubject.next(response);
      })
    );
  }

  logout() {
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse } from '../../shared/models/auth.model';

@Injectable({ providedIn: 'root' })
export class Auth {

  private baseUrl = 'http://localhost:8080/api/auth';
  private isBrowser: boolean;

  // Don't call loadUser() here — browser isn't guaranteed yet
  private currentUser$ = new BehaviorSubject<AuthResponse | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.currentUser$.next(this.loadUser()); // safe now — isBrowser is set
  }

  login(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(res => this.saveUser(res))
    );
  }

  register(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, data).pipe(
      tap(res => this.saveUser(res))
    );
  }

  saveUser(res: AuthResponse): void {
    if (this.isBrowser) {
      localStorage.setItem('token', res.token);
      localStorage.setItem('role', res.role);
      localStorage.setItem('user', JSON.stringify(res));
    }
    this.currentUser$.next(res);
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem('token') : null;
  }

  getRole(): string | null {
    return this.isBrowser ? localStorage.getItem('role') : null;
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUser$.value;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getRole() === 'ROLE_ADMIN';
  }

  logout(): void {
    if (this.isBrowser) localStorage.clear();
    this.currentUser$.next(null);
    this.router.navigate(['/login']);
  }

  private loadUser(): AuthResponse | null {
    if (!this.isBrowser) return null;  // SSR guard — no localStorage on server
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  }
}

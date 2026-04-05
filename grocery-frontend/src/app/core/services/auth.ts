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

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);

    // 🔥 restore user ON APP START
    const user = this.loadUserFromStorage();
    if (user) {
      this.currentUserSubject.next(user);
    }
  }

  // ───────── AUTH APIs ─────────

  login(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(res => this.handleAuthSuccess(res))
    );
  }

  register(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, data).pipe(
      tap(res => this.handleAuthSuccess(res))
    );
  }

  // ───────── STATE MANAGEMENT ─────────

  private handleAuthSuccess(res: AuthResponse): void {
    if (this.isBrowser) {
      localStorage.setItem('token', res.token);
      localStorage.setItem('role', res.role);
      localStorage.setItem('user', JSON.stringify(res));
    }
    this.currentUserSubject.next(res);
  }

  private loadUserFromStorage(): AuthResponse | null {
    if (!this.isBrowser) return null;

    const stored = localStorage.getItem('user');
    if (!stored) return null;

    try {
      return JSON.parse(stored);
    } catch {
      return null;
    }
  }

  // ───────── GETTERS ─────────

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem('token') : null;
  }

  getRole(): string | null {
    return this.isBrowser ? localStorage.getItem('role') : null;
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.getToken(); // 🔥 أهم تعديل
  }

  isAdmin(): boolean {
    return this.getRole() === 'ROLE_ADMIN';
  }

  // ───────── LOGOUT ─────────

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('user');
    }

    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }
}

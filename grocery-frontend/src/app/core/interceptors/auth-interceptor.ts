import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  intercept(
    req: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {

    // ✅ get token safely
    const token = this.isBrowser ? localStorage.getItem('token') : null;

    // ✅ attach token if exists
    const authReq = token
      ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
      : req;

    return next.handle(authReq).pipe(
      catchError((err: HttpErrorResponse) => {

        // 🔥 IMPORTANT: do NOT logout or redirect here
        if (err.status === 401) {
          console.warn('401 Unauthorized:', req.url);

          // ❌ no localStorage.clear()
          // ❌ no navigation
          // let Guard handle auth state
        }

        return throwError(() => err);
      })
    );
  }
}

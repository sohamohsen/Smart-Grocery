import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router      = inject(Router);
  const platformId  = inject(PLATFORM_ID);

  // On the server there's no localStorage — don't redirect
  if (!isPlatformBrowser(platformId)) return true;

  const token = localStorage.getItem('token');
  if (token) return true;

  router.navigate(['/login']);
  return false;
};

export const adminGuard: CanActivateFn = () => {
  const router     = inject(Router);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) return true;

  const token = localStorage.getItem('token');
  const role  = localStorage.getItem('role');

  if (token && role === 'ROLE_ADMIN') return true;

  router.navigate(['/login']);
  return false;
};

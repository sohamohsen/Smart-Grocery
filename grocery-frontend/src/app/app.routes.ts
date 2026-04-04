// app.routes.ts — add these routes alongside your existing login route

import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { RegisterUser } from './auth/register/register';
import { RegisterAdmin } from './auth/register-admin/register-admin';
// import your guards as needed
// import { AuthGuard } from './core/guards/auth.guard';
// import { AdminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '',          redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',    component: Login },

  // User self-registration — publicly accessible
  { path: 'register/user',  component: RegisterUser },

  // Admin registration — protect this in production!
  // Option A: leave open during development
  { path: 'register/admin', component: RegisterAdmin },

  // Option B: guard it so only existing admins can create more admins
  // { path: 'register/admin', component: RegisterAdmin, canActivate: [AdminGuard] },

  // Your existing authenticated routes below…
  // { path: 'products', component: ProductList, canActivate: [AuthGuard] },
  // { path: 'admin',    component: AdminDashboard, canActivate: [AdminGuard] },

  { path: '**', redirectTo: 'login' },
];

import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { AdminLayoutComponent } from './shared/layouts/admin-layout/admin-layout.component';
import { UserLayoutComponent } from './shared/layouts/user-layout/user-layout.component';
import { adminGuard } from './core/guards/admin.guard';
import { superAdminGuard } from './core/guards/super-admin.guard';
import { userGuard } from './core/guards/user.guard';

import { RegisterComponent } from './features/auth/register/register.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'products', loadComponent: () => import('./features/admin/products/products.component').then(m => m.ProductsComponent) },
      { path: 'categories', loadComponent: () => import('./features/admin/category/category.component').then(m => m.CategoryComponent) },
      { path: 'categories/add', loadComponent: () => import('./features/admin/add-category/add-category.component').then(m => m.AddCategoryComponent) },
      { path: 'suggestions', loadComponent: () => import('./features/admin/suggestions/suggestions.component').then(m => m.SuggestionsComponent) }
    ]
  },
  {
    path: 'super-admin',
    component: AdminLayoutComponent,
    canActivate: [superAdminGuard],
    children: [
      { path: '', redirectTo: 'accounts', pathMatch: 'full' },
      { path: 'admins', redirectTo: 'accounts', pathMatch: 'full' },
      { path: 'accounts', loadComponent: () => import('./features/super-admin/admins/admins.component').then(m => m.SuperAdminAdminsComponent) }
    ]
  },
  {
    path: 'user',
    component: UserLayoutComponent,
    canActivate: [userGuard],
    children: [
      { path: '', redirectTo: 'products', pathMatch: 'full' },
      { path: 'products', loadComponent: () => import('./features/user/user-products/user-products.component').then(m => m.UserProductsComponent) },
      { path: 'wishlist', loadComponent: () => import('./features/user/wishlist/wishlist.component').then(m => m.WishlistComponent) }
    ]
  },
  { path: '**', redirectTo: 'login' }
];

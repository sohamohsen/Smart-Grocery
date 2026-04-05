import { authGuard, adminGuard } from './core/guards/auth-guard';
import {AdminCategories} from './admin/categories/categories';
import {AdminProducts} from './admin/products/products';
import {AdminLayout} from './admin/dashboard/dashboard';
import {UserLayout} from './user/layout/layout';
import {RegisterAdmin} from './auth/register-admin/register-admin';
import {RegisterUser} from './auth/register/register';
import {Login} from './auth/login/login';
import {Routes} from '@angular/router';
import {Products} from './user/products/products';
import {ProductDetail} from './user/product-details/product-details';
import {Wishlist} from './core/services/wishlist';

export const routes: Routes = [
  { path: '',               redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',          component: Login },
  { path: 'register/user',  component: RegisterUser },
  { path: 'register/admin', component: RegisterAdmin },

  // ── User pages — protected ──
  {
    path: '',
    component: UserLayout,
    canActivate: [authGuard],          // ← add this
    children: [
      { path: 'products',     component: Products },
      { path: 'products/:id', component: ProductDetail },
      { path: 'wishlist',     component: Wishlist },
    ]
  },

  // ── Admin pages — protected ──
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [adminGuard],         // ← add this
    children: [
      { path: '',           redirectTo: 'products', pathMatch: 'full' },
      { path: 'products',   component: AdminProducts },
      { path: 'categories', component: AdminCategories },
    ]
  },

  { path: '**', redirectTo: 'login' }
];

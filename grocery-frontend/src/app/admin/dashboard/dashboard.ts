import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../core/services/auth';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class AdminLayout {
  sidebarOpen = true;

  navItems = [
    { icon: '📊', label: 'Overview',   route: '/admin/overview' },
    { icon: '📦', label: 'Products',   route: '/admin/products' },
    { icon: '🏷️', label: 'Categories', route: '/admin/categories' },
  ];

  constructor(private auth: Auth, private router: Router) {}

  get username(): string { return this.auth.getCurrentUser()?.username ?? 'Admin'; }

  logout()        { this.auth.logout(); }
  toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; }
}

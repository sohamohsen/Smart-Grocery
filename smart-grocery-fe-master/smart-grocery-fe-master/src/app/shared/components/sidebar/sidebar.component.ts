import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  get isSuperAdmin(): boolean {
    return this.authService.isSuperAdminRole();
  }

  get canAccessAdminArea(): boolean {
    return this.authService.canAccessAdminArea();
  }

  get panelTitle(): string {
    return this.isSuperAdmin ? 'Super Admin Panel' : 'Admin Panel';
  }

  get userName(): string {
    return this.authService.currentUser?.username || 'Guest';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

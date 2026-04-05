import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../core/services/auth';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, FormsModule],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class UserLayout {
  menuOpen = false;

  constructor(private auth: Auth, private router: Router) {}

  get username(): string { return this.auth.getCurrentUser()?.username ?? 'Guest'; }

  logout() { this.auth.logout(); }
}

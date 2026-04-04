import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthResponse } from '../../shared/models/auth.model';
import { Auth } from '../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  form = { username: '', password: '' };
  loading      = false;
  errorMessage = '';
  hidePassword = true;

  constructor(private authService: Auth, private router: Router) {}

  login() {
    if (!this.form.username || !this.form.password) return;
    this.loading      = true;
    this.errorMessage = '';

    this.authService.login(this.form).subscribe({
      next: (res: AuthResponse) => {
        if (res.role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/products']);
        }
      },
      error: () => {
        this.loading      = false;
        this.errorMessage = 'Wrong username or password. Please try again.';
      }
    });
  }
}

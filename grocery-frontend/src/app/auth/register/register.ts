import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../core/services/auth';

@Component({
  selector: 'app-register-user',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterUser {

  form = {
    name: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'USER'
  };

  loading        = false;
  errorMessage   = '';
  hidePassword   = true;
  hideConfirm    = true;
  successMessage = '';

  constructor(private authService: Auth, private router: Router) {}

  get passwordMismatch(): boolean {
    return this.form.password !== this.form.confirmPassword &&
      this.form.confirmPassword.length > 0;
  }

  get formValid(): boolean {
    return !!this.form.name &&
      !!this.form.username &&
      !!this.form.email &&
      this.form.password.length >= 6 &&
      this.form.password === this.form.confirmPassword;
  }

  register() {
    if (!this.formValid) return;
    this.loading      = true;
    this.errorMessage = '';

    const { confirmPassword, ...payload } = this.form;

    this.authService.register(payload).subscribe({
      next: () => {
        this.successMessage = 'Account created! Redirecting…';
        setTimeout(() => this.router.navigate(['/products']), 1200);
      },
      error: (err) => {
        this.loading      = false;
        this.errorMessage = err?.error?.message || 'Registration failed. Please try again.';
      }
    });
  }
}

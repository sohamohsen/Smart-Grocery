import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../core/services/auth';

@Component({
  selector: 'app-register-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register-admin.html',
  styleUrl: './register-admin.css',
})
export class RegisterAdmin {

  form = {
    name: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'ADMIN'
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
        this.successMessage = 'Admin account created! Redirecting to dashboard…';
        setTimeout(() => this.router.navigate(['/admin']), 1200);
      },
      error: (err) => {
        this.loading      = false;
        this.errorMessage = err?.error?.message || 'Registration failed. Please try again.';
      }
    });
  }
}

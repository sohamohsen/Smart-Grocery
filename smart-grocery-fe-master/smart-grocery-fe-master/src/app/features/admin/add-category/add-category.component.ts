import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CategoryService } from '../../../core/services/category.service';

@Component({
  selector: 'app-add-category',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-category.component.html',
  styleUrls: ['./add-category.component.scss']
})
export class AddCategoryComponent {
  categoryForm: FormGroup;
  error: string = '';
  success: string = '';
  loading: boolean = false;

  private fb = inject(FormBuilder);
  private categoryService = inject(CategoryService);
  private router = inject(Router);

  constructor() {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  onSubmit() {
    if (this.categoryForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.success = '';

    this.categoryService.createCategory(this.categoryForm.value).subscribe({
      next: () => {
        this.success = 'Category added successfully!';
        this.loading = false;
        this.categoryForm.reset();
        
        // Redirect back to the categories tab after a short delay
        setTimeout(() => {
           this.router.navigate(['/admin/categories']);
        }, 1500);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to add category. Please try again.';
        this.loading = false;
      }
    });
  }
}

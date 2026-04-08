import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CategoryService } from '../../../core/services/category.service';
import { AuthService } from '../../../core/services/auth.service';
import { CategoryRequest, CategoryResponse, PageResponse } from '../../../core/models/api.models';
import { UI_TEXT } from '../../../core/constants/ui-text.constants';

@Component({
  selector: 'app-category',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent implements OnInit {
  private categoryService = inject(CategoryService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

  readonly UI_TEXT = UI_TEXT;

  categoriesPage: PageResponse<CategoryResponse> | null = null;
  loading = false;
  error = '';

  currentPage = 0;
  pageSize = 10;
  searchQuery = '';
  showDeleted = false;

  showModal = false;
  categoryForm: FormGroup;
  editingCategoryId: number | null = null;
  submitError = '';

  constructor() {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.loading = true;
    this.error = '';

    const request$ = this.showDeleted && this.isSuperAdmin
      ? this.categoryService.getDeletedCategories(this.currentPage, this.pageSize)
      : this.categoryService.getCategories(
          this.currentPage,
          this.pageSize,
          this.searchQuery || undefined
        );

    request$.subscribe({
      next: (res) => {
        this.categoriesPage = res.data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load categories. Please try again.';
        this.loading = false;
      }
    });
  }

  onSearch(event: Event) {
    this.searchQuery = (event.target as HTMLInputElement).value.trim();
    this.currentPage = 0;
    this.loadCategories();
  }

  changePage(dir: number) {
    this.currentPage += dir;
    this.loadCategories();
  }

  get isSuperAdmin(): boolean {
    return this.authService.isSuperAdminRole();
  }

  toggleDeletedView() {
    this.showDeleted = !this.showDeleted;
    this.currentPage = 0;
    this.searchQuery = '';
    this.loadCategories();
  }

  openModal(category?: CategoryResponse) {
    this.submitError = '';

    if (category) {
      this.editingCategoryId = category.id;
      this.categoryForm.patchValue({
        name: category.name,
        description: category.description || ''
      });
    } else {
      this.editingCategoryId = null;
      this.categoryForm.reset({
        name: '',
        description: ''
      });
    }

    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  saveCategory() {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    this.submitError = '';
    const request: CategoryRequest = {
      name: this.categoryForm.value.name.trim(),
      description: this.categoryForm.value.description?.trim() || ''
    };

    const request$ = this.editingCategoryId
      ? this.categoryService.updateCategory(this.editingCategoryId, request)
      : this.categoryService.createCategory(request);

    request$.subscribe({
      next: () => {
        this.closeModal();
        this.loadCategories();
      },
      error: (err) => {
        this.submitError = err.error?.message || 'Failed to save category. Please try again.';
      }
    });
  }

  openCategoryProducts(category: CategoryResponse) {
    if (this.showDeleted) {
      return;
    }

    this.router.navigate(['/admin/products'], {
      queryParams: {
        categoryId: category.id,
        categoryName: category.name
      }
    });
  }

  trackByCategoryId(_: number, category: CategoryResponse): number {
    return category.id;
  }

  deleteCategory(id: number) {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe(() => this.loadCategories());
    }
  }

  restoreCategory(id: number) {
    if (confirm('Are you sure you want to restore this category?')) {
      this.categoryService.restoreCategory(id).subscribe(() => this.loadCategories());
    }
  }

}

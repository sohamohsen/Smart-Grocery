import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { AuthService } from '../../../core/services/auth.service';
import { ProductResponse, CategoryResponse, PageResponse } from '../../../core/models/api.models';
import { UI_TEXT } from '../../../core/constants/ui-text.constants';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly UI_TEXT = UI_TEXT;

  productsPage: PageResponse<ProductResponse> | null = null;
  categories: CategoryResponse[] = [];
  
  currentPage = 0;
  pageSize = 10;
  searchQuery = '';
  selectedCategoryId: number | undefined;
  selectedCategoryName = '';
  showDeleted = false;
  
  showModal = false;
  productForm: FormGroup;
  editingId: number | null = null;
  submitError = '';

  constructor() {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      categoryId: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(0)]],
      brand: [''],
      barcode: [''],
      imageUrl: [''],
      description: ['']
    });
  }

  ngOnInit() {
    this.loadCategories();
    this.route.queryParamMap.subscribe((params) => {
      const categoryIdParam = params.get('categoryId');
      const parsedCategoryId = categoryIdParam ? parseInt(categoryIdParam, 10) : NaN;

      this.selectedCategoryId = Number.isNaN(parsedCategoryId) ? undefined : parsedCategoryId;
      this.selectedCategoryName = params.get('categoryName') || '';
      this.currentPage = 0;

      if (this.selectedCategoryId && !this.selectedCategoryName) {
        this.syncSelectedCategoryName();
      }

      this.loadProducts();
    });
  }

  loadProducts() {
    this.productService.getAdminProducts(
      this.currentPage,
      this.pageSize,
      this.searchQuery || undefined,
      this.selectedCategoryId,
      undefined,
      this.showDeleted
    ).subscribe({
      next: (res: any) => this.productsPage = res.data,
      error: (err: any) => console.error(err)
    });
  }

  loadCategories() {
    this.categoryService.getCategories(0, 100).subscribe((res: any) => {
      this.categories = res.data.content;
      this.syncSelectedCategoryName();
    });
  }

  onSearch(event: any) {
    this.searchQuery = event.target.value;
    this.currentPage = 0;
    this.loadProducts();
  }

  onCategoryFilterChange(event: Event) {
    const val = (event.target as HTMLSelectElement).value;
    const categoryId = val ? parseInt(val, 10) : undefined;
    const categoryName = categoryId
      ? this.categories.find((category) => category.id === categoryId)?.name
      : undefined;

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        categoryId: categoryId ?? null,
        categoryName: categoryName ?? null
      },
      queryParamsHandling: 'merge'
    });
  }

  clearCategoryFilter() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        categoryId: null,
        categoryName: null
      },
      queryParamsHandling: 'merge'
    });
  }

  changePage(dir: number) {
    this.currentPage += dir;
    this.loadProducts();
  }

  get isSuperAdmin(): boolean {
    return this.authService.isSuperAdminRole();
  }

  toggleDeletedView() {
    this.showDeleted = !this.showDeleted;
    this.currentPage = 0;
    this.searchQuery = '';

    if (this.showDeleted && this.selectedCategoryId) {
      this.clearCategoryFilter();
      return;
    }

    this.loadProducts();
  }

  openModal(product?: ProductResponse) {
    this.submitError = '';
    if (product) {
      this.editingId = product.id;
      this.productForm.patchValue(product);
    } else {
      this.editingId = null;
      this.productForm.reset();
    }
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  saveProduct() {
    if (this.productForm.invalid) return;
    const req = { ...this.productForm.value, categoryId: parseInt(this.productForm.value.categoryId, 10) };
    
    if (this.editingId) {
      this.productService.updateProduct(this.editingId, req).subscribe({
        next: () => { this.closeModal(); this.loadProducts(); },
        error: (err: any) => this.submitError = err.error?.message || 'Error updating product'
      });
    } else {
      this.productService.addProduct(req).subscribe({
        next: () => { this.closeModal(); this.loadProducts(); },
        error: (err: any) => this.submitError = err.error?.message || 'Error adding product'
      });
    }
  }

  deleteProduct(id: number) {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id).subscribe(() => this.loadProducts());
    }
  }

  restoreProduct(id: number) {
    if (confirm('Are you sure you want to restore this product?')) {
      this.productService.restoreProduct(id).subscribe(() => this.loadProducts());
    }
  }

  private syncSelectedCategoryName() {
    if (!this.selectedCategoryId || this.selectedCategoryName) {
      return;
    }

    this.selectedCategoryName =
      this.categories.find((category) => category.id === this.selectedCategoryId)?.name || '';
  }
}

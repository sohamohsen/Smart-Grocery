import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { CategoryResponse } from '../../../core/models/api.models';
import { UI_TEXT } from '../../../core/constants/ui-text.constants';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  readonly UI_TEXT = UI_TEXT;
  
  totalProducts = 0;
  totalCategories = 0;
  categories: CategoryResponse[] = [];
  categoriesLoading = true;
  categoriesError = '';
  categorySearchQuery = '';
  
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private router = inject(Router);
  private readonly categoryPreviewSize = 8;

  ngOnInit() {
    this.productService.getAdminProducts(0, 1).subscribe({
      next: (res) => this.totalProducts = res.data.totalElements,
      error: () => console.error('Failed to load products count')
    });

    this.loadCategorySummary();
    this.loadCategories();
  }

  loadCategories() {
    this.categoriesLoading = true;
    this.categoriesError = '';

    this.categoryService.getCategories(
      0,
      this.categoryPreviewSize,
      this.categorySearchQuery || undefined
    ).subscribe({
      next: (res) => {
        this.categories = res.data.content;
        this.categoriesLoading = false;
      },
      error: () => {
        this.categoriesError = 'Failed to load categories.';
        this.categoriesLoading = false;
        console.error('Failed to load categories.');
      }
    });
  }

  onCategorySearch(query: string) {
    this.categorySearchQuery = query.trim();
    this.loadCategories();
  }

  clearCategorySearch() {
    this.categorySearchQuery = '';
    this.loadCategories();
  }

  openCategoryProducts(category: CategoryResponse) {
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

  private loadCategorySummary() {
    this.categoryService.getCategories(0, 1).subscribe({
      next: (res) => this.totalCategories = res.data.totalElements,
      error: () => console.error('Failed to load categories count')
    });
  }
}

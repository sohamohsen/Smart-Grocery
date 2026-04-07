import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { ProductResponse, CategoryResponse, PageResponse } from '../../../core/models/api.models';

@Component({
  selector: 'app-user-products',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-products.component.html',
  styleUrls: ['./user-products.component.scss']
})
export class UserProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private wishlistService = inject(WishlistService);

  productsPage: PageResponse<ProductResponse> | null = null;
  categories: CategoryResponse[] = [];
  wishlistIds = new Set<number>();

  loading = false;
  error = '';
  currentPage = 0;
  pageSize = 12;
  searchQuery = '';
  selectedCategoryId: number | undefined;

  ngOnInit() {
    this.loadProducts();
    this.loadCategories();
    this.loadWishlistIds();
  }

  loadProducts() {
    this.loading = true;
    this.error = '';
    this.productService.getUserProducts(
      this.currentPage, this.pageSize,
      this.searchQuery || undefined,
      this.selectedCategoryId
    ).subscribe({
      next: (res) => { this.productsPage = res.data; this.loading = false; },
      error: () => {
        this.error = 'Failed to load products. Please try again.';
        this.loading = false;
      }
    });
  }

  loadCategories() {
    this.categoryService.getCategories(0, 100).subscribe((res: any) => {
      this.categories = res.data.content;
    });
  }

  loadWishlistIds() {
    this.wishlistService.getWishlist(0, 1000).subscribe({
      next: (res: any) => {
        this.wishlistIds = new Set(res.data.content.map((f: any) => f.productId));
      },
      error: () => {}
    });
  }

  onSearch(event: Event) {
    this.searchQuery = (event.target as HTMLInputElement).value.trim();
    this.currentPage = 0;
    this.loadProducts();
  }

  onCategoryChange(event: Event) {
    const val = (event.target as HTMLSelectElement).value;
    this.selectCategory(val ? parseInt(val, 10) : undefined);
  }

  selectCategory(categoryId?: number) {
    this.selectedCategoryId = categoryId;
    this.currentPage = 0;
    this.loadProducts();
  }

  clearFilters() {
    this.searchQuery = '';
    this.selectedCategoryId = undefined;
    this.currentPage = 0;
    this.loadProducts();
  }

  toggleWishlist(productId: number) {
    this.wishlistService.toggleFavorite(productId).subscribe(() => {
      const updated = new Set(this.wishlistIds);
      if (updated.has(productId)) {
        updated.delete(productId);
      } else {
        updated.add(productId);
      }
      this.wishlistIds = updated;
    });
  }

  isInWishlist(productId: number): boolean {
    return this.wishlistIds.has(productId);
  }

  changePage(dir: number) {
    this.currentPage += dir;
    this.loadProducts();
  }

  get selectedCategoryName(): string {
    return this.categories.find((category) => category.id === this.selectedCategoryId)?.name || '';
  }

  trackByCategoryId(_: number, category: CategoryResponse): number {
    return category.id;
  }

  trackByProductId(_: number, product: ProductResponse): number {
    return product.id;
  }
}

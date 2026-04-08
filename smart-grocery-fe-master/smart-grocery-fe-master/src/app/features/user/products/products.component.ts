import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../../core/services/product.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { ProductResponse, PageResponse } from '../../../core/models/api.models';
import { FormsModule } from '@angular/forms';
import { UI_TEXT } from '../../../core/constants/ui-text.constants';

@Component({
  selector: 'app-user-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html'
})
export class ProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private wishlistService = inject(WishlistService);

  readonly UI_TEXT = UI_TEXT;

  productsPage: PageResponse<ProductResponse> | null = null;
  favoriteIds = new Set<number>();
  
  currentPage = 0;
  pageSize = 12;
  searchQuery = '';

  ngOnInit() {
    this.loadFavorites();
    this.loadProducts();
  }

  loadFavorites() {
    this.wishlistService.getWishlist(0, 1000).subscribe((res: any) => {
      this.favoriteIds = new Set(res.data.content.map((f: any) => f.product.id));
    });
  }

  loadProducts() {
    this.productService.getUserProducts(this.currentPage, this.pageSize, this.searchQuery).subscribe((res: any) => {
      this.productsPage = res.data;
    });
  }

  onSearch() {
    this.currentPage = 0;
    this.loadProducts();
  }

  changePage(dir: number) {
    this.currentPage += dir;
    this.loadProducts();
  }

  toggleFavorite(productId: number) {
    this.wishlistService.toggleFavorite(productId).subscribe(() => {
      if (this.favoriteIds.has(productId)) {
        this.favoriteIds.delete(productId);
      } else {
        this.favoriteIds.add(productId);
      }
    });
  }

  isFavorite(productId: number): boolean {
    return this.favoriteIds.has(productId);
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserProductService, WishlistService, ProductResponse, CategoryResponse } from '../../core/services/user';
import { debounceTime, Subject } from 'rxjs';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './products.html',
  styleUrl: './products.css',
})
export class Products implements OnInit {
  products: ProductResponse[] = [];
  categories: CategoryResponse[] = [];

  page = 0; size = 12; totalPages = 0; totalElements = 0;
  search = ''; selectedCategory: number | '' = '';
  sortBy = 'createdAt'; sortDir = 'desc';

  loading = false;
  wishlistMap: Record<number, boolean> = {};
  togglingMap: Record<number, boolean> = {};
  toast = ''; toastErr = false;

  private searchSubject = new Subject<string>();

  constructor(
    private productSvc: UserProductService,
    private wishlistSvc: WishlistService
  ) {}

  ngOnInit() {
    this.loadCategories();
    this.loadProducts();
    // debounce search input
    this.searchSubject.pipe(debounceTime(400)).subscribe(() => {
      this.page = 0; this.loadProducts();
    });
  }

  loadProducts() {
    this.loading = true;
    this.productSvc.getProducts({
      page: this.page, size: this.size,
      search: this.search || undefined,
      categoryId: this.selectedCategory !== '' ? +this.selectedCategory : undefined,
      sortBy: this.sortBy, sortDir: this.sortDir,
    }).subscribe({
      next: r => {
        this.products      = r.data.content;
        this.totalPages    = r.data.totalPages;
        this.totalElements = r.data.totalElements;
        this.loading       = false;
        this.loadWishlistStates();
      },
      error: () => { this.loading = false; }
    });
  }

  loadCategories() {
    this.productSvc.getCategories().subscribe({
      next: r => this.categories = r.data.content
    });
  }

  loadWishlistStates() {
    this.products.forEach(p => {
      this.wishlistSvc.isInWishlist(p.id).subscribe({
        next: r => this.wishlistMap[p.id] = r.data
      });
    });
  }

  onSearchChange() { this.searchSubject.next(this.search); }
  onFilterChange() { this.page = 0; this.loadProducts(); }
  onSortChange()   { this.page = 0; this.loadProducts(); }
  prevPage()       { if (this.page > 0)                    { this.page--; this.loadProducts(); } }
  nextPage()       { if (this.page < this.totalPages - 1)  { this.page++; this.loadProducts(); } }

  toggleWishlist(p: ProductResponse, event: Event) {
    event.preventDefault(); event.stopPropagation();
    if (this.togglingMap[p.id]) return;
    this.togglingMap[p.id] = true;
    this.wishlistSvc.toggle(p.id).subscribe({
      next: r => {
        // backend returns the item if added, or a removal message
        const wasIn = this.wishlistMap[p.id];
        this.wishlistMap[p.id] = !wasIn;
        this.togglingMap[p.id] = false;
        this.flash(wasIn ? 'Removed from wishlist' : '❤️ Added to wishlist!');
      },
      error: () => { this.togglingMap[p.id] = false; this.flash('Failed', true); }
    });
  }

  flash(msg: string, err = false) {
    this.toast = msg; this.toastErr = err;
    setTimeout(() => this.toast = '', 2800);
  }

  trackById(_: number, p: ProductResponse) { return p.id; }
}

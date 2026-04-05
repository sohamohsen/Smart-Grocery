import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WishlistService, FavoriteResponse } from '../../core/services/user';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './wishlist.html',
  styleUrl: './wishlist.css',
})
export class Wishlist implements OnInit {
  items: FavoriteResponse[] = [];
  page = 0; size = 12; totalPages = 0; totalElements = 0;
  loading  = true;
  removing: Record<number, boolean> = {};
  toast = ''; toastErr = false;

  constructor(private wishlistSvc: WishlistService) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.wishlistSvc.getWishlist(this.page, this.size).subscribe({
      next: r => {
        this.items         = r.data.content;
        this.totalPages    = r.data.totalPages;
        this.totalElements = r.data.totalElements;
        this.loading       = false;
      },
      error: () => { this.loading = false; }
    });
  }

  remove(item: FavoriteResponse) {
    if (this.removing[item.productId]) return;
    this.removing[item.productId] = true;
    this.wishlistSvc.toggle(item.productId).subscribe({
      next: () => {
        this.items = this.items.filter(i => i.productId !== item.productId);
        this.totalElements--;
        this.removing[item.productId] = false;
        this.flash('Removed from wishlist');
      },
      error: () => { this.removing[item.productId] = false; this.flash('Failed to remove', true); }
    });
  }

  prevPage() { if (this.page > 0) { this.page--; this.load(); } }
  nextPage() { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }

  flash(msg: string, err = false) {
    this.toast = msg; this.toastErr = err;
    setTimeout(() => this.toast = '', 2800);
  }
}

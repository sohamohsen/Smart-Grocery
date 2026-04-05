import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UserProductService, WishlistService, ProductResponse } from '../../core/services/user';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-details.html',
  styleUrl: './product-details.css',
})
export class ProductDetail implements OnInit {
  product: ProductResponse | null = null;
  loading   = true;
  inWishlist = false;
  toggling   = false;
  toast = ''; toastErr = false;

  constructor(
    private route: ActivatedRoute,
    private productSvc: UserProductService,
    private wishlistSvc: WishlistService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productSvc.getProduct(id).subscribe({
      next: r => {
        this.product = r.data;
        this.loading = false;
        this.checkWishlist(id);
      },
      error: () => { this.loading = false; }
    });
  }

  checkWishlist(id: number) {
    this.wishlistSvc.isInWishlist(id).subscribe({
      next: r => this.inWishlist = r.data
    });
  }

  toggleWishlist() {
    if (!this.product || this.toggling) return;
    this.toggling = true;
    this.wishlistSvc.toggle(this.product.id).subscribe({
      next: () => {
        this.inWishlist = !this.inWishlist;
        this.toggling   = false;
        this.flash(this.inWishlist ? '❤️ Added to wishlist!' : 'Removed from wishlist');
      },
      error: () => { this.toggling = false; this.flash('Failed', true); }
    });
  }

  flash(msg: string, err = false) {
    this.toast = msg; this.toastErr = err;
    setTimeout(() => this.toast = '', 2800);
  }
}

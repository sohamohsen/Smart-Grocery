import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WishlistService } from '../../../core/services/wishlist.service';
import { FavoriteResponse } from '../../../core/models/api.models';
import { UI_TEXT } from '../../../core/constants/ui-text.constants';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './wishlist.component.html',
  styleUrls: ['./wishlist.component.scss']
})
export class WishlistComponent implements OnInit {
  private wishlistService = inject(WishlistService);
  
  readonly UI_TEXT = UI_TEXT;
  
  favorites: FavoriteResponse[] = [];
  loading = true;

  ngOnInit() {
    this.loadFavorites();
  }

  loadFavorites() {
    this.loading = true;
    this.wishlistService.getWishlist(0, 100).subscribe({
      next: (res: any) => {
        this.favorites = res.data.content;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  removeFavorite(productId: number) {
    this.wishlistService.toggleFavorite(productId).subscribe(() => {
      this.favorites = this.favorites.filter(f => f.productId !== productId);
    });
  }
}

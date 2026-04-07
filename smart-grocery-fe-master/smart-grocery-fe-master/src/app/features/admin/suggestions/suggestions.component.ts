import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { ProductRequest, CategoryResponse } from '../../../core/models/api.models';

@Component({
  selector: 'app-suggestions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './suggestions.component.html',
  styleUrls: ['./suggestions.component.scss']
})
export class SuggestionsComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private fb = inject(FormBuilder);

  searchForm: FormGroup;
  categories: CategoryResponse[] = [];
  
  loading = false;
  suggestionResult: ProductRequest | null = null;
  bulkQueue: ProductRequest[] = [];
  error = '';
  success = '';

  constructor() {
    this.searchForm = this.fb.group({
      barcode: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(0)]],
      categoryId: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.categoryService.getCategories(0, 100).subscribe((res: any) => {
      this.categories = res.data.content;
    });
  }

  onSearch() {
    if (this.searchForm.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';
    this.suggestionResult = null;

    const { barcode, price, categoryId } = this.searchForm.value;

    this.productService.fetchSuggestion(barcode, parseFloat(price), parseInt(categoryId, 10)).subscribe({
      next: (res: any) => {
        this.suggestionResult = res.data;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Failed to fetch suggestion. Ensure barcode is valid.';
        this.loading = false;
      }
    });
  }

  addToDb() {
    if (!this.suggestionResult) return;
    this.loading = true;
    this.productService.addProduct(this.suggestionResult).subscribe({
      next: () => {
        this.success = 'Product successfully added to database!';
        this.suggestionResult = null;
        this.loading = false;
        this.searchForm.reset();
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Failed to add product';
        this.loading = false;
      }
    });
  }

  addToBulkQueue() {
    if (!this.suggestionResult) return;

    const alreadyQueued = this.bulkQueue.some((item) =>
      item.barcode
        ? item.barcode === this.suggestionResult?.barcode
        : item.name === this.suggestionResult?.name && item.categoryId === this.suggestionResult?.categoryId
    );

    if (alreadyQueued) {
      this.success = 'This suggestion is already queued for bulk add.';
      this.error = '';
      return;
    }

    this.bulkQueue = [...this.bulkQueue, { ...this.suggestionResult }];
    this.success = 'Suggestion added to the bulk queue.';
    this.error = '';
    this.suggestionResult = null;
  }

  removeQueuedProduct(index: number) {
    this.bulkQueue = this.bulkQueue.filter((_, itemIndex) => itemIndex !== index);
  }

  submitBulkQueue() {
    if (!this.bulkQueue.length) return;

    this.loading = true;
    this.error = '';
    this.success = '';

    this.productService.bulkAddProducts(this.bulkQueue).subscribe({
      next: (res: any) => {
        this.success = res.message || 'Queued products added successfully.';
        this.bulkQueue = [];
        this.loading = false;
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Failed to bulk add queued products.';
        this.loading = false;
      }
    });
  }
}

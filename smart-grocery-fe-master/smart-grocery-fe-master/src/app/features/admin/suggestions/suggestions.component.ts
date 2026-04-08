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
      barcode: [''],
      name: ['', Validators.required],
      brand: [''],
      price: ['', [Validators.required, Validators.min(0.01)]],
      categoryId: [null, Validators.required],
      description: [''],
      imageUrl: ['']
    });
  }

  ngOnInit() {
    this.categoryService.getCategories(0, 100).subscribe((res: any) => {
      this.categories = res.data.content;
    });
  }

  // =========================
  // MESSAGE HANDLER
  // =========================
  private setMessage(type: 'error' | 'success', message: string) {
    this.error = type === 'error' ? message : '';
    this.success = type === 'success' ? message : '';

    setTimeout(() => {
      this.error = '';
      this.success = '';
    }, 3000);
  }

  // =========================
  // FETCH PRODUCT
  // =========================
  onSearch() {
    const { barcode } = this.searchForm.value;

    if (!barcode || !barcode.trim()) {
      this.setMessage('error', 'Please enter a barcode first.');
      return;
    }

    this.loading = true;
    this.suggestionResult = null;

    const price = Number(this.searchForm.value.price) || 0;
    const categoryId = Number(this.searchForm.value.categoryId) || 1;

    this.productService.fetchSuggestion(barcode, price, categoryId).subscribe({
      next: (res: any) => {
        const result = res.data;
        this.suggestionResult = result;

        this.searchForm.patchValue({
          name: result.name,
          brand: result.brand,
          description: result.description,
          imageUrl: result.imageUrl
        });

        this.loading = false;
      },
      error: () => {
        this.setMessage('error', 'Failed to fetch product.');
        this.loading = false;
      }
    });
  }

  // =========================
  // ADD TO DATABASE
  // =========================
  addToDb() {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      this.setMessage('error', 'Please fill all required fields.');
      return;
    }

    this.loading = true;

    const formValue = this.searchForm.value;

    const productData: ProductRequest = {
      ...this.suggestionResult,
      ...formValue,
      categoryId: Number(formValue.categoryId),
      price: Number(formValue.price)
    };

    this.productService.addProduct(productData).subscribe({
      next: () => {
        this.setMessage('success', 'Product added successfully ✅');
        this.resetForm();
        this.loading = false;
      },
      error: (err: any) => {
        this.handleApiError(err);
        this.loading = false;
      }
    });
  }

  // =========================
  // ADD TO BULK QUEUE
  // =========================
  addToBulkQueue() {
    if (this.searchForm.invalid) {
      this.setMessage('error', 'Fill required fields before adding.');
      return;
    }

    const formValue = this.searchForm.value;

    const product: ProductRequest = {
      ...this.suggestionResult,
      ...formValue,
      categoryId: Number(formValue.categoryId),
      price: Number(formValue.price)
    };

    const exists = this.bulkQueue.some(item =>
      item.barcode
        ? item.barcode === product.barcode
        : item.name === product.name && item.categoryId === product.categoryId
    );

    if (exists) {
      this.setMessage('error', 'Product already exists in queue.');
      return;
    }

    this.bulkQueue.push(product);
    this.setMessage('success', 'Added to bulk queue 📦');
  }

  removeQueuedProduct(index: number) {
    this.bulkQueue.splice(index, 1);
  }

  submitBulkQueue() {
    if (!this.bulkQueue.length) {
      this.setMessage('error', 'Queue is empty.');
      return;
    }

    this.loading = true;

    this.productService.bulkAddProducts(this.bulkQueue).subscribe({
      next: () => {
        this.setMessage('success', 'All products added successfully 🚀');
        this.bulkQueue = [];
        this.loading = false;
      },
      error: (err: any) => {
        this.handleApiError(err);
        this.loading = false;
      }
    });
  }

  // =========================
  // ERROR HANDLING
  // =========================
  private handleApiError(err: any) {
    const msg = err.error?.message || '';

    if (msg.toLowerCase().includes('exist') || msg.toLowerCase().includes('duplicate')) {
      this.setMessage('error', 'Product already exists ❗');
    } else if (msg.toLowerCase().includes('validation')) {
      this.setMessage('error', 'Invalid data. Please check inputs.');
    } else {
      this.setMessage('error', 'Something went wrong.');
    }
  }

  // =========================
  // RESET FORM
  // =========================
  private resetForm() {
    this.searchForm.reset({
      barcode: '',
      name: '',
      brand: '',
      price: '',
      categoryId: null,
      description: '',
      imageUrl: ''
    });

    this.suggestionResult = null;
  }
}

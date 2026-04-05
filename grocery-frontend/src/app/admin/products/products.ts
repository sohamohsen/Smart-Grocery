import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AdminProductService, AdminCategoryService,
  ProductResponse, ProductRequest, CategoryResponse
} from '../../core/services/admin';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.html',
  styleUrl: './products.css',
})
export class AdminProducts implements OnInit {

  products: ProductResponse[] = [];
  categories: CategoryResponse[] = [];

  // Pagination / filters
  page = 0; size = 10; totalPages = 0; totalElements = 0;
  search = '';
  filterCategory: number | '' = '';
  filterApproved: string = '';
  filterDeleted  = false;

  loading = false; saving = false;
  toast = ''; toastError = false;

  // Modals: 'add' | 'edit' | 'bulk' | 'fetch' | null
  modal: string | null = null;
  editingId: number | null = null;

  form: ProductRequest = this.blank();
  tagsStr = '';

  // Bulk add
  bulkJson = ''; bulkError = ''; bulkParsed: ProductRequest[] = [];

  // Fetch by barcode
  fetchBarcode = ''; fetchPrice = 0; fetchCategoryId: number | '' = '';
  fetchLoading = false; fetchResult: ProductRequest | null = null;

  constructor(
    private productSvc: AdminProductService,
    private categorySvc: AdminCategoryService
  ) {}

  ngOnInit() { this.loadCategories(); this.loadProducts(); }

  blank(): ProductRequest {
    return { name:'', description:'', brand:'', price:0, barcode:'', imageUrl:'', categoryId:0, tags:[] };
  }

  // ── Load ─────────────────────────────────────────────────────
  loadProducts() {
    this.loading = true;
    this.productSvc.getProducts({
      page: this.page, size: this.size,
      search: this.search || undefined,
      categoryId: this.filterCategory !== '' ? +this.filterCategory : undefined,
      isApproved: this.filterApproved !== '' ? this.filterApproved === 'true' : undefined,
      isDeleted: this.filterDeleted || undefined,
    }).subscribe({
      next: r => { this.products = r.data.content; this.totalPages = r.data.totalPages; this.totalElements = r.data.totalElements; this.loading = false; },
      error: () => { this.loading = false; this.flash('Failed to load products', true); }
    });
  }

  loadCategories() {
    this.categorySvc.getCategories().subscribe({ next: r => this.categories = r.data.content });
  }

  applyFilters() { this.page = 0; this.loadProducts(); }
  prevPage() { if (this.page > 0) { this.page--; this.loadProducts(); } }
  nextPage() { if (this.page < this.totalPages - 1) { this.page++; this.loadProducts(); } }

  // ── Add / Edit ───────────────────────────────────────────────
  openAdd() { this.form = this.blank(); this.tagsStr = ''; this.editingId = null; this.modal = 'add'; }

  openEdit(p: ProductResponse) {
    this.editingId = p.id;
    this.form = { name: p.name, description: p.description??'', brand: p.brand??'', price: p.price, barcode: p.barcode??'', imageUrl: p.imageUrl??'', categoryId: p.categoryId, tags: [...(p.tags??[])] };
    this.tagsStr = (p.tags ?? []).join(', ');
    this.modal = 'edit';
  }

  onTagsChange(v: string) { this.form.tags = v.split(',').map(t => t.trim()).filter(Boolean); }

  saveProduct() {
    this.form.tags = this.tagsStr.split(',').map(t => t.trim()).filter(Boolean);
    this.saving = true;
    const obs = this.editingId != null
      ? this.productSvc.updateProduct(this.editingId, this.form)
      : this.productSvc.addProduct(this.form);
    obs.subscribe({
      next: () => { this.saving = false; this.modal = null; this.flash(this.editingId ? 'Product updated ✓' : 'Product added ✓'); this.loadProducts(); },
      error: () => { this.saving = false; this.flash('Save failed', true); }
    });
  }

  // ── Delete / Restore ─────────────────────────────────────────
  deleteProduct(p: ProductResponse) {
    if (!confirm(`Delete "${p.name}"?`)) return;
    this.productSvc.deleteProduct(p.id).subscribe({
      next: () => { this.flash('Deleted'); this.loadProducts(); },
      error: () => this.flash('Delete failed', true)
    });
  }

  restoreProduct(p: ProductResponse) {
    this.productSvc.restoreProduct(p.id).subscribe({
      next: () => { this.flash('Restored ✓'); this.loadProducts(); },
      error: () => this.flash('Restore failed', true)
    });
  }

  // ── Bulk add ─────────────────────────────────────────────────
  openBulk() { this.bulkJson = ''; this.bulkError = ''; this.bulkParsed = []; this.modal = 'bulk'; }

  parseBulk() {
    this.bulkError = '';
    try {
      const p = JSON.parse(this.bulkJson);
      if (!Array.isArray(p)) throw new Error('Must be a JSON array [ {...}, {...} ]');
      this.bulkParsed = p;
    } catch (e: any) { this.bulkError = e.message; this.bulkParsed = []; }
  }

  submitBulk() {
    if (!this.bulkParsed.length) return;
    this.saving = true;
    this.productSvc.bulkAddProducts(this.bulkParsed).subscribe({
      next: r => { this.saving = false; this.modal = null; this.flash(`${r.data.length} products added ✓`); this.loadProducts(); },
      error: () => { this.saving = false; this.flash('Bulk add failed', true); }
    });
  }

  // ── Fetch by barcode ─────────────────────────────────────────
  openFetch() { this.fetchBarcode = ''; this.fetchPrice = 0; this.fetchCategoryId = ''; this.fetchResult = null; this.modal = 'fetch'; }

  fetchSuggestion() {
    if (!this.fetchBarcode || this.fetchCategoryId === '') return;
    this.fetchLoading = true; this.fetchResult = null;
    this.productSvc.fetchSuggestion(this.fetchBarcode, this.fetchPrice, +this.fetchCategoryId).subscribe({
      next: r => { this.fetchResult = r.data; this.fetchLoading = false; },
      error: () => { this.fetchLoading = false; this.flash('Barcode not found', true); }
    });
  }

  useFetchResult() {
    if (!this.fetchResult) return;
    this.form = { ...this.fetchResult };
    this.tagsStr = (this.fetchResult.tags ?? []).join(', ');
    this.editingId = null; this.modal = 'add';
  }

  flash(msg: string, err = false) {
    this.toast = msg; this.toastError = err;
    setTimeout(() => this.toast = '', 3200);
  }

  closeModal() { this.modal = null; }
}

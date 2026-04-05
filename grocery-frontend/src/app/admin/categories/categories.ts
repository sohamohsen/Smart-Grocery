import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminCategoryService, CategoryResponse, CategoryRequest } from '../../core/services/admin';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories.html',
  styleUrl: './categories.css',
})
export class AdminCategories implements OnInit {

  categories: CategoryResponse[]        = [];
  deletedCategories: CategoryResponse[] = [];

  page = 0; size = 10; totalPages = 0; totalElements = 0;
  deletedPage = 0; deletedTotalPages = 0;

  loading        = false;
  saving         = false;
  showDeleted    = false;
  toast          = '';
  toastError     = false;

  modal: 'add' | 'edit' | null = null;
  editingId: number | null = null;
  form: CategoryRequest = { name: '', description: '' };

  constructor(private categorySvc: AdminCategoryService) {}

  ngOnInit() { this.loadCategories(); }

  loadCategories() {
    this.loading = true;
    this.categorySvc.getCategories(this.page, this.size).subscribe({
      next: r => { this.categories = r.data.content; this.totalPages = r.data.totalPages; this.totalElements = r.data.totalElements; this.loading = false; },
      error: () => { this.loading = false; this.flash('Failed to load categories', true); }
    });
  }

  loadDeleted() {
    this.categorySvc.getDeletedCategories(this.deletedPage, this.size).subscribe({
      next: r => { this.deletedCategories = r.data.content; this.deletedTotalPages = r.data.totalPages; }
    });
  }

  toggleDeleted() {
    this.showDeleted = !this.showDeleted;
    if (this.showDeleted) this.loadDeleted();
  }

  prevPage() { if (this.page > 0) { this.page--; this.loadCategories(); } }
  nextPage() { if (this.page < this.totalPages - 1) { this.page++; this.loadCategories(); } }

  openAdd() { this.form = { name: '', description: '' }; this.editingId = null; this.modal = 'add'; }

  openEdit(c: CategoryResponse) {
    this.editingId = c.id;
    this.form = { name: c.name, description: c.description ?? '' };
    this.modal = 'edit';
  }

  save() {
    this.saving = true;
    const obs = this.editingId != null
      ? this.categorySvc.updateCategory(this.editingId, this.form)
      : this.categorySvc.addCategory(this.form);
    obs.subscribe({
      next: () => { this.saving = false; this.modal = null; this.flash(this.editingId ? 'Category updated ✓' : 'Category added ✓'); this.loadCategories(); },
      error: () => { this.saving = false; this.flash('Save failed', true); }
    });
  }

  delete(c: CategoryResponse) {
    if (!confirm(`Delete category "${c.name}"?`)) return;
    this.categorySvc.deleteCategory(c.id).subscribe({
      next: () => { this.flash('Category deleted'); this.loadCategories(); if (this.showDeleted) this.loadDeleted(); },
      error: () => this.flash('Delete failed', true)
    });
  }

  restore(c: CategoryResponse) {
    this.categorySvc.restoreCategory(c.id).subscribe({
      next: () => { this.flash('Restored ✓'); this.loadCategories(); this.loadDeleted(); },
      error: () => this.flash('Restore failed', true)
    });
  }

  flash(msg: string, err = false) {
    this.toast = msg; this.toastError = err;
    setTimeout(() => this.toast = '', 3200);
  }

  closeModal() { this.modal = null; }
}

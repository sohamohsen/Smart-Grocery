import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  AdminSummaryResponse,
  AuditLogResponse,
  FavoriteResponse,
  PageResponse,
  RegisterRequest
} from '../../../core/models/api.models';
import { SuperAdminService } from '../../../core/services/super-admin.service';

type ManageableRole = 'ADMIN' | 'USER';

@Component({
  selector: 'app-super-admin-admins',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admins.component.html',
  styleUrls: ['./admins.component.scss']
})
export class SuperAdminAdminsComponent implements OnInit {
  accountsPage: PageResponse<AdminSummaryResponse> | null = null;
  loading = false;
  error = '';
  currentPage = 0;
  pageSize = 6;
  searchQuery = '';
  selectedRole: ManageableRole = 'ADMIN';
  totalAdmins = 0;
  totalUsers = 0;
  actionMessage = '';
  private updatingIds = new Set<number>();
  selectedAccount: AdminSummaryResponse | null = null;
  adminActionsPage: PageResponse<AuditLogResponse> | null = null;
  wishlistPage: PageResponse<FavoriteResponse> | null = null;
  detailLoading = false;
  detailError = '';

  adminForm: FormGroup;
  creating = false;
  submitError = '';
  submitSuccess = '';

  private superAdminService = inject(SuperAdminService);
  private fb = inject(FormBuilder);

  constructor() {
    this.adminForm = this.fb.group({
      name: ['', Validators.required],
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.loadSummary();
    this.loadAccounts();
  }

  get currentRoleLabel(): string {
    return this.selectedRole === 'ADMIN' ? 'Admins' : 'Users';
  }

  get isAdminTab(): boolean {
    return this.selectedRole === 'ADMIN';
  }

  get isSelectedAdmin(): boolean {
    return this.selectedAccount?.role === 'ADMIN';
  }

  get isSelectedUser(): boolean {
    return this.selectedAccount?.role === 'USER';
  }

  loadAccounts(): void {
    this.loading = true;
    this.error = '';

    const request$ = this.selectedRole === 'ADMIN'
      ? this.superAdminService.getAdmins(
          this.currentPage,
          this.pageSize,
          this.searchQuery || undefined
        )
      : this.superAdminService.getUsers(
          this.currentPage,
          this.pageSize,
          this.searchQuery || undefined
        );

    request$.subscribe({
      next: (res) => {
        this.accountsPage = res.data;
        this.loading = false;
      },
      error: () => {
        this.error = `Failed to load ${this.currentRoleLabel.toLowerCase()}. Please try again.`;
        this.loading = false;
      }
    });
  }

  loadSummary(): void {
    forkJoin({
      admins: this.superAdminService.getAdmins(0, 1),
      users: this.superAdminService.getUsers(0, 1)
    }).subscribe({
      next: ({ admins, users }) => {
        this.totalAdmins = admins.data.totalElements;
        this.totalUsers = users.data.totalElements;
      },
      error: () => {
        this.totalAdmins = 0;
        this.totalUsers = 0;
      }
    });
  }

  selectRole(role: ManageableRole): void {
    if (this.selectedRole === role) {
      return;
    }

    this.selectedRole = role;
    this.currentPage = 0;
    this.searchQuery = '';
    this.error = '';
    this.actionMessage = '';
    this.clearSelection();
    this.loadAccounts();
  }

  onSearch(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value.trim();
    this.currentPage = 0;
    this.actionMessage = '';
    this.loadAccounts();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.currentPage = 0;
    this.actionMessage = '';
    this.loadAccounts();
  }

  changePage(direction: number): void {
    if (!this.accountsPage) {
      return;
    }

    const nextPage = this.currentPage + direction;
    if (nextPage < 0 || nextPage >= this.accountsPage.totalPages) {
      return;
    }

    this.currentPage = nextPage;
    this.loadAccounts();
  }

  selectAccount(account: AdminSummaryResponse): void {
    this.selectedAccount = account;
    this.adminActionsPage = null;
    this.wishlistPage = null;
    this.detailError = '';
    this.loadSelectedAccountDetails();
  }

  createAdmin(): void {
    if (this.adminForm.invalid) {
      this.adminForm.markAllAsTouched();
      return;
    }

    this.creating = true;
    this.submitError = '';
    this.submitSuccess = '';

    const request: RegisterRequest = {
      name: this.adminForm.value.name.trim(),
      username: this.adminForm.value.username.trim(),
      email: this.adminForm.value.email.trim(),
      password: this.adminForm.value.password
    };

    this.superAdminService.createAdmin(request).subscribe({
      next: (res) => {
        this.creating = false;
        this.submitSuccess = `${res.data.name} was added successfully.`;
        this.adminForm.reset({
          name: '',
          username: '',
          email: '',
          password: ''
        });
        this.actionMessage = 'Admin account created successfully.';
        this.currentPage = 0;
        this.selectedRole = 'ADMIN';
        this.clearSelection();
        this.loadSummary();
        this.loadAccounts();
      },
      error: (err) => {
        this.creating = false;
        this.submitError = err.error?.message || 'Failed to add admin. Please try again.';
      }
    });
  }

  toggleAccountStatus(account: AdminSummaryResponse): void {
    if (this.updatingIds.has(account.id)) {
      return;
    }

    this.updatingIds.add(account.id);
    this.actionMessage = '';
    this.error = '';

    this.superAdminService.updateAccountStatus(account.id, !account.active).subscribe({
      next: (res) => {
        const accountType = res.data.role.toLowerCase().replace('_', ' ');
        this.actionMessage = `${res.data.name} is now ${res.data.active ? 'active' : 'inactive'} as ${accountType}.`;
        this.updatingIds.delete(account.id);
        if (this.selectedAccount?.id === account.id) {
          this.selectedAccount = res.data;
          this.loadSelectedAccountDetails();
        }
        this.loadSummary();
        this.loadAccounts();
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to update account status. Please try again.';
        this.updatingIds.delete(account.id);
      }
    });
  }

  isUpdating(accountId: number): boolean {
    return this.updatingIds.has(accountId);
  }

  trackByAccountId(_: number, account: AdminSummaryResponse): number {
    return account.id;
  }

  private loadSelectedAccountDetails(): void {
    if (!this.selectedAccount) {
      return;
    }

    this.detailLoading = true;
    this.detailError = '';

    if (this.selectedAccount.role === 'ADMIN') {
      this.superAdminService.getAdminActions(this.selectedAccount.id).subscribe({
        next: (res) => {
          this.adminActionsPage = res.data;
          this.wishlistPage = null;
          this.detailLoading = false;
        },
        error: () => {
          this.detailError = 'Failed to load admin actions.';
          this.detailLoading = false;
        }
      });
      return;
    }

    this.superAdminService.getUserWishlist(this.selectedAccount.id).subscribe({
      next: (res) => {
        this.wishlistPage = res.data;
        this.adminActionsPage = null;
        this.detailLoading = false;
      },
      error: () => {
        this.detailError = 'Failed to load user wishlist.';
        this.detailLoading = false;
      }
    });
  }

  private clearSelection(): void {
    this.selectedAccount = null;
    this.adminActionsPage = null;
    this.wishlistPage = null;
    this.detailError = '';
    this.detailLoading = false;
  }
}

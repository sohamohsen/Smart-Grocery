export interface AuthResponse {
  token: string;
  role: string;
  username: string;
}

export interface LoginRequest {
  username?: string;
  password?: string;
}

export interface RegisterRequest {
  name?: string;
  username?: string;
  email?: string;
  password?: string;
  role?: string;
}

export interface ProductResponse {
  id: number;
  name: string;
  description: string;
  brand: string;
  price: number;
  barcode: string;
  imageUrl: string;
  isApproved: boolean;
  categoryId: number;
  categoryName: string;
  tags: string[];
  createdAt: string;
}

export interface ProductRequest {
  name: string;
  categoryId: number;
  price: number;
  brand?: string;
  barcode?: string;
  imageUrl?: string;
  description?: string;
  tags?: string[];
}

export interface CategoryResponse {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
}

export interface AdminSummaryResponse {
  id: number;
  name: string;
  username: string;
  email: string;
  active: boolean;
  role: string;
  createdAt: string;
}

export interface AuditLogResponse {
  id: number;
  action: string;
  entityType: string;
  description: string;
  oldValue?: string | null;
  newValue?: string | null;
  ipAddress?: string | null;
  createdAt: string;
}

export interface FavoriteResponse {
  id: number;
  productId: number;
  productName: string;
  productBrand: string;
  productPrice: number;
  productImageUrl: string;
  categoryName: string;
  tags: string[];
  addedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiResponse<T> {
  status: string | number;
  message: string;
  data: T;
}

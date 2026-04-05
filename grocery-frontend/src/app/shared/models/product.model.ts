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
  createdAt: string; // ISO date from backend
}

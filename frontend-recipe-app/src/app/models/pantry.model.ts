export interface PantryItem {
  id?: number;
  name: string;
  quantity: number;
  unit: string;
  expiryDate: string;
  addedDate?: string;
  runningLow?: boolean;
}

export interface AddPantryItemRequest {
  name: string;
  quantity: number;
  unit: string;
  expiryDate: string;
}
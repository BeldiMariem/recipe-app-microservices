import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PantryService } from '../../services/pantry.service';
import { PantryItem, AddPantryItemRequest } from '../../models/pantry.model';
import { RecipeService } from '../../services/recipe.service';

@Component({
    selector: 'app-pantry',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
    templateUrl: './pantry.component.html',
    styleUrls: ['./pantry.component.scss']
})
export class PantryComponent implements OnInit {
    private pantryService = inject(PantryService);
    private recipeService = inject(RecipeService);
    private fb = inject(FormBuilder);
    private cdr = inject(ChangeDetectorRef);

    pantryItems: PantryItem[] = [];
    filteredItems: PantryItem[] = [];
    expiringItems: PantryItem[] = [];

    searchQuery = '';
    selectedCategory = 'all';
    sortBy = 'name';
    categories: string[] = [
        'Dairy', 'Meat', 'Vegetables', 'Fruits', 'Grains',
        'Spices', 'Oils', 'Canned', 'Baking', 'Beverages'
    ];

    showModal = false;
    editingItem: PantryItem | null = null;
    pantryForm: FormGroup;

    constructor() {
        this.pantryForm = this.fb.group({
            name: ['', Validators.required],
            quantity: [1, [Validators.required, Validators.min(0.1)]],
            unit: ['', Validators.required],
            expiryDate: ['', Validators.required],
            category: ['']
        });
    }

    ngOnInit() {
        this.loadPantryItems();
    }

    loadPantryItems(): void {
        this.pantryService.getPantryItems().subscribe({
            next: (items) => {
                this.pantryItems = items;
                this.filterItems();
                this.updateExpiringItems();
                this.cdr.detectChanges();
            },
            error: (error) => {
                console.error('Error loading pantry items:', error);
                this.cdr.detectChanges();

            }
        });
    }

    filterItems(): void {
        let filtered = [...this.pantryItems];

        if (this.searchQuery) {
            const query = this.searchQuery.toLowerCase();
            filtered = filtered.filter(item =>
                item.name.toLowerCase().includes(query)
            );
        }

        if (this.selectedCategory !== 'all') {
            filtered = filtered.filter(item =>
                this.getItemCategory(item.name) === this.selectedCategory
            );
        }

        switch (this.sortBy) {
            case 'name':
                filtered.sort((a, b) => a.name.localeCompare(b.name));
                break;
            case 'expiry':
                filtered.sort((a, b) => new Date(a.expiryDate).getTime() - new Date(b.expiryDate).getTime());
                break;
            case 'added':
                filtered.sort((a, b) => {
                    const dateA = a.addedDate ? new Date(a.addedDate).getTime() : 0;
                    const dateB = b.addedDate ? new Date(b.addedDate).getTime() : 0;
                    return dateB - dateA;
                });
                break;
            case 'quantity':
                filtered.sort((a, b) => b.quantity - a.quantity);
                break;
        }

        this.filteredItems = filtered;
    }

    updateExpiringItems(): void {
        const now = new Date();
        const twoWeeksFromNow = new Date(now.getTime() + 14 * 24 * 60 * 60 * 1000);

        this.expiringItems = this.pantryItems.filter(item => {
            const expiryDate = new Date(item.expiryDate);
            this.cdr.detectChanges();

            return expiryDate <= twoWeeksFromNow && expiryDate >= now;
        }).slice(0, 5);
    }

    getExpiringSoonCount(): number {
        const now = new Date();
        const twoWeeksFromNow = new Date(now.getTime() + 14 * 24 * 60 * 60 * 1000);

        return this.pantryItems.filter(item => {
            const expiryDate = new Date(item.expiryDate);
            return expiryDate <= twoWeeksFromNow && expiryDate >= now;
            this.cdr.detectChanges();

        }).length;
    }

    getLowStockCount(): number {
        return this.pantryItems.filter(item => item.quantity < 2).length;
    }

    getCategoryCount(): number {
        const categories = new Set(this.pantryItems.map(item => this.getItemCategory(item.name)));
        return categories.size;
    }

    getItemCategory(itemName: string): string {
        const name = itemName.toLowerCase();

        if (name.includes('milk') || name.includes('cheese') || name.includes('yogurt') || name.includes('butter')|| name.includes('egg')) {
            return 'Dairy';
        } else if (name.includes('chicken') || name.includes('beef') || name.includes('pork') || name.includes('fish') ) {
            return 'Meat';
        } else if (name.includes('tomato') || name.includes('onion') || name.includes('garlic') || name.includes('pepper')|| name.includes('carrot')) {
            return 'Vegetables';
        } else if (name.includes('apple') || name.includes('banana') || name.includes('orange') || name.includes('berry')|| name.includes('strawberry')) {
            return 'Fruits';
        } else if (name.includes('rice') || name.includes('pasta') || name.includes('flour') || name.includes('bread')) {
            return 'Grains';
        } else if (name.includes('salt') || name.includes('pepper') || name.includes('cumin') || name.includes('paprika')) {
            return 'Spices';
        }

        return 'Other';
    }

    getExpiryClass(expiryDate: string): string {
        const now = new Date();
        const expiry = new Date(expiryDate);
        const daysUntilExpiry = Math.floor((expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));

        if (daysUntilExpiry < 0) return 'expired';
        if (daysUntilExpiry < 3) return 'critical';
        if (daysUntilExpiry < 7) return 'warning';
        return 'safe';
    }

    isExpiringSoon(expiryDate: string): boolean {
        const now = new Date();
        const expiry = new Date(expiryDate);
        const daysUntilExpiry = Math.floor((expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
        return daysUntilExpiry >= 0 && daysUntilExpiry < 7;
    }

    getDaysUntilExpiry(expiryDate: string): number {
        const now = new Date();
        const expiry = new Date(expiryDate);
        return Math.ceil((expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    }

    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    getStockIcon(item: PantryItem): string {
        if (item.quantity <= 0.5) return 'bi-exclamation-triangle text-danger';
        if (item.quantity <= 1) return 'bi-exclamation text-warning';
        return 'bi-check-circle text-success';
    }

    getStockStatus(item: PantryItem): string {
        if (item.quantity <= 0.5) return 'Very Low';
        if (item.quantity <= 1) return 'Low';
        if (item.quantity <= 3) return 'Moderate';
        return 'Good';
    }

    getStockPercentage(item: PantryItem): number {
        const maxStock = 10;
        return Math.min((item.quantity / maxStock) * 100, 100);
    }

    openAddItemModal(): void {
        this.editingItem = null;
        this.pantryForm.reset({
            quantity: 1,
            unit: '',
            category: ''
        });
        this.cdr.detectChanges();

        this.showModal = true;
    }

    editItem(item: PantryItem): void {
        this.editingItem = item;
        this.pantryForm.patchValue({
            name: item.name,
            quantity: item.quantity,
            unit: item.unit,
            expiryDate: item.expiryDate.split('T')[0], 
            category: this.getItemCategory(item.name)

        });
        this.cdr.detectChanges();

        this.showModal = true;
    }

    closeModal(): void {
        this.showModal = false;
        this.editingItem = null;
        this.pantryForm.reset();
    }

    saveItem(): void {
        if (this.pantryForm.invalid) return;

        const formValue = this.pantryForm.value;
        const pantryItem: AddPantryItemRequest = {
            name: formValue.name,
            quantity: formValue.quantity,
            unit: formValue.unit,
            expiryDate: formValue.expiryDate
        };

        if (this.editingItem) {
            this.pantryService.updatePantryItem(this.editingItem.id!, pantryItem).subscribe({
                next: () => {
                    this.loadPantryItems();
                    this.closeModal();
                },
                error: (error) => console.error('Error updating item:', error)
            });
        } else {
            this.pantryService.addPantryItem(pantryItem).subscribe({
                next: () => {
                    this.loadPantryItems();
                    this.closeModal();
                },
                error: (error) => console.error('Error adding item:', error)
            });
        }
    }

    deleteItem(item: PantryItem): void {
        if (confirm(`Are you sure you want to remove "${item.name}" from your pantry?`)) {
            this.loadPantryItems();
        }
    }

    findRecipesWithItem(item: PantryItem): void {
        console.log('Finding recipes with:', item.name);
    }

    exportPantry(): void {
        const csvContent = this.convertToCSV(this.pantryItems);
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'my-pantry.csv';
        a.click();
        window.URL.revokeObjectURL(url);
    }

    private convertToCSV(items: PantryItem[]): string {
        const headers = ['Name', 'Quantity', 'Unit', 'Expiry Date', 'Added Date'];
        const rows = items.map(item => [
            `"${item.name}"`,
            item.quantity,
            item.unit,
            item.expiryDate,
            item.addedDate || ''
        ]);

        return [headers.join(','), ...rows.map(row => row.join(','))].join('\n');
    }
}
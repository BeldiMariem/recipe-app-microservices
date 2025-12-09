import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  
  currentUser: any;
  cookingTips = [
    {
      icon: 'bi-thermometer',
      title: 'Perfect Temperature',
      content: 'Let meat rest at room temperature for 30 minutes before cooking for even cooking.'
    },
    {
      icon: 'bi-droplet',
      title: 'Stay Hydrated',
      content: 'Keep a glass of water nearby while cooking to stay hydrated and focused.'
    },
    {
      icon: 'bi-clock-history',
      title: 'Prep Ahead',
      content: 'Chop vegetables and measure ingredients before you start cooking.'
    },
    {
      icon: 'bi-shield-check',
      title: 'Safety First',
      content: 'Always use a sharp knife - dull knives are more dangerous and require more pressure.'
    },
    {
      icon: 'bi-flower1',
      title: 'Fresh Herbs',
      content: 'Add fresh herbs at the end of cooking to preserve their flavor and aroma.'
    }
  ];
  
  recentActivity = [
    {
      icon: 'bi-check-circle',
      type: 'success',
      title: 'Added ingredients to pantry',
      description: 'You added tomatoes, onions, and garlic',
      time: '2 hours ago'
    },
    {
      icon: 'bi-bookmark',
      type: 'primary',
      title: 'Saved a recipe',
      description: '"Mediterranean Bowl" added to your favorites',
      time: 'Yesterday'
    },
    {
      icon: 'bi-exclamation-triangle',
      type: 'warning',
      title: 'Expiration alert',
      description: 'Milk and eggs are expiring in 2 days',
      time: '2 days ago'
    }
  ];

  ngOnInit() {
    this.currentUser = this.authService.getCurrentUser();
  }

  navigateTo(path: string, queryParams?: any): void {
    this.router.navigate([path], { queryParams });
  }

  refreshTips(): void {
    this.cookingTips = [...this.cookingTips].sort(() => Math.random() - 0.5);
  }

  getRandomTip(): any {
    return this.cookingTips[Math.floor(Math.random() * this.cookingTips.length)];
  }
}
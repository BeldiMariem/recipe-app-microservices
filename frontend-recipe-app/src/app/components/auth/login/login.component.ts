import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';


@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
 private authService = inject(AuthService);
  private router = inject(Router);
  
  credentials = {
    username: '',
    password: ''
  };
  isLoading = false;

  onSubmit() {
    if (this.isLoading) return;
    
    this.isLoading = true;
    this.authService.login(this.credentials).subscribe({
      next: () => {
        console.log('Login successful!');
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Invalid username or password:', error);
        alert('Invalid username or password');
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}

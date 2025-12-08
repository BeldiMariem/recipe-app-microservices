import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  
  userData = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: ''
  };
  isLoading = false;

getPasswordStrength(): string {
  const password = this.userData.password;
  if (!password) return '';
  
  const hasMinLength = password.length >= 6;
  const hasUppercase = /[A-Z]/.test(password);
  const hasNumber = /\d/.test(password);
  const hasSpecial = /[!@#$%^&*]/.test(password);
  
  const strength = [hasMinLength, hasUppercase, hasNumber, hasSpecial].filter(Boolean).length;
  
  if (strength <= 1) return 'weak';
  if (strength <= 3) return 'medium';
  return 'strong';
}

getPasswordStrengthText(): string {
  const strength = this.getPasswordStrength();
  switch (strength) {
    case 'weak': return 'Weak password';
    case 'medium': return 'Medium password';
    case 'strong': return 'Strong password';
    default: return '';
  }
}

hasMinLength(): boolean {
  return this.userData.password.length >= 6;
}

hasUppercase(): boolean {
  return /[A-Z]/.test(this.userData.password);
}

hasNumber(): boolean {
  return /\d/.test(this.userData.password);
}

hasSpecial(): boolean {
  return /[!@#$%^&*]/.test(this.userData.password);
}
  onSubmit() {
    if (this.isLoading) return;
    
    this.isLoading = true;
    this.authService.register(this.userData).subscribe({
      next: () => {
        console.log('Registration successful!');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Registration failed:', error);
        alert('Registration failed. Please try again.');
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}
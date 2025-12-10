import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { RecipeDetailsComponent } from './components/recipe-details/recipe-details.component';
import { PantryComponent } from './components/pantry/pantry.component';
import { RecipesComponent } from './components/recipes/recipes.component';
import { AiChefComponent } from './components/ai-chef/ai-chef.component';

export const routes: Routes = [
  { 
    path: '', 
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent) 
  },
  { 
    path: 'login', 
    loadComponent: () => import('./components/auth/login/login.component').then(m => m.LoginComponent) 
  },
  { 
    path: 'register', 
    loadComponent: () => import('./components/auth/register/register.component').then(m => m.RegisterComponent) 
  },
  { path: 'recipes/:id', component: RecipeDetailsComponent },
  { path: 'pantry', component: PantryComponent },
  { path: 'recipes', component: RecipesComponent },
  { path: 'ai-chef', component: AiChefComponent },

  { 
    path: 'dashboard', 
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '' }
];
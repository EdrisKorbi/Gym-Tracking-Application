import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private router = inject(Router);

  // UI State Signals
  currentUser = signal<User | null>(null);
  isLoading = signal<boolean>(true);
  isDarkMode = signal<boolean>(false);

  ngOnInit(): void {
    this.loadFreshProfile();

    // Theme Engine Init
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.isDarkMode.set(true);
      document.documentElement.setAttribute('data-theme', 'dark');
    }
  }

  loadFreshProfile(): void {
    this.userService.getProfile().subscribe({
      next: (response) => {
        this.currentUser.set(response.data);
        this.isLoading.set(false);
      },
      error: () => {
        // If the backend rejects the request, force logout
        this.logout();
      }
    });
  }

  toggleTheme() {
    this.isDarkMode.update(mode => !mode);

    if (this.isDarkMode()) {
      document.documentElement.setAttribute('data-theme', 'dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
      localStorage.setItem('theme', 'light');
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']); // Safely routes the user away
  }
}
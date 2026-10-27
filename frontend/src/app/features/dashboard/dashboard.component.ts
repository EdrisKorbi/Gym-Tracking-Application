import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router'; // RouterModule includes RouterOutlet

// Services
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';

// Models
import { User } from '../../core/models/user.model';

// UI Components
import { NeuButtonComponent } from '../../shared/ui/neu-button/neu-button.component';
import { NeuSegmentedComponent } from '../../shared/ui/neu-segmented/neu-segmented.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule, // Required for <router-outlet>
    NeuButtonComponent,
    NeuSegmentedComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private router = inject(Router);

  // Core State Signals
  currentUser = signal<User | null>(null);
  isLoading = signal<boolean>(true);
  isDarkMode = signal<boolean>(false);

  // Navigation Signals
  topTabs = ['Overview', 'Workouts', 'Nutrition'];
  selectedTopTab = signal<string>('Overview');
  rangeOptions = ['30 days', '90 days', 'This year'];
  selectedRange = signal<string>('30 days');
  activeSideIcon = signal<string>('analytics');

  ngOnInit(): void {
    this.loadFreshProfile();

    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.isDarkMode.set(true);
      document.documentElement.setAttribute('data-theme', 'dark');
    }
  }

  // 3. Add the navigation method
  goToAdmin(): void {
    this.router.navigate(['/admin']);
  }

  loadFreshProfile(): void {
    this.userService.getProfile().subscribe({
      next: (response) => {
        this.currentUser.set(response.data);
        this.authService.syncProfile(response.data);
        this.isLoading.set(false);
      },
      error: () => this.logout()
    });
  }

  onTopTabChange(tab: string): void {
    this.selectedTopTab.set(tab);
    // Future enterprise hook: Navigate based on tab selection if needed
    if (tab === 'Workouts') {
      this.router.navigate(['/workouts']);
      this.activeSideIcon.set('workouts');
    } else if (tab === 'Overview') {
      this.router.navigate(['/overview']);
      this.activeSideIcon.set('grid');
    }
  }

  onRangeChange(range: string): void {
    this.selectedRange.set(range);
  }

  setTheme(dark: boolean): void {
    if (this.isDarkMode() === dark) return;

    const applyTheme = () => {
      this.isDarkMode.set(dark);
      if (dark) {
        document.documentElement.setAttribute('data-theme', 'dark');
        localStorage.setItem('theme', 'dark');
      } else {
        document.documentElement.removeAttribute('data-theme');
        localStorage.setItem('theme', 'light');
      }
    };

    const doc = document as Document & { startViewTransition?: (cb: () => void) => void };
    if (doc.startViewTransition) {
      doc.startViewTransition(() => applyTheme());
    } else {
      applyTheme();
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
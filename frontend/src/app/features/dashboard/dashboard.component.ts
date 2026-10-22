import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterModule],
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

  ngOnInit(): void {
    this.loadFreshProfile();
  }

  loadFreshProfile(): void {
    this.userService.getProfile().subscribe({
      next: (response) => {
        this.currentUser.set(response.data);
        this.isLoading.set(false);
      },
      error: () => {
        // If the backend rejects the request (e.g. user was banned mid-session), force logout
        this.logout();
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
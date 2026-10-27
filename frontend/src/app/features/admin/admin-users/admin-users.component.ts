import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

import { AdminService } from '../../../core/services/admin.service';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';
import { NeuButtonComponent } from '../../../shared/ui/neu-button/neu-button.component';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, NeuButtonComponent, DatePipe],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly userService = inject(UserService);

  // --- Core State ---
  currentAdminId = signal<string | null>(null);
  users = signal<User[]>([]);

  // --- Pagination State ---
  currentPage = signal<number>(0);
  pageSize = signal<number>(20);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  // --- UI Loading & Error State ---
  isPageLoading = signal<boolean>(true);
  updatingUserId = signal<string | null>(null); // Mutex lock for row-level actions
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.initializeAdminContext();
  }

  /**
   * Initializes the view by fetching the current admin's profile (for self-protection logic)
   * and then loading the first page of users.
   */
  private initializeAdminContext(): void {
    this.userService.getProfile().subscribe({
      next: (response) => {
        this.currentAdminId.set(response.data.id);
        this.loadUsers(this.currentPage());
      },
      error: (error: HttpErrorResponse) => {
        this.handleError('Failed to verify admin identity.', error);
        this.isPageLoading.set(false);
      }
    });
  }

  /**
   * Fetches a paginated slice of the user base from PostgreSQL.
   */
  loadUsers(pageIndex: number): void {
    this.isPageLoading.set(true);
    this.errorMessage.set(null);

    this.adminService.getAllUsers(pageIndex, this.pageSize(), 'createdAt,desc')
      .pipe(finalize(() => this.isPageLoading.set(false)))
      .subscribe({
        next: (response) => {
          // CORRECTED: Mapping directly from response.data (which IS the Page<T> object)
          this.users.set(response.data.content);
          this.currentPage.set(response.data.number);
          this.totalElements.set(response.data.totalElements);
          this.totalPages.set(response.data.totalPages);
        },
        error: (error: HttpErrorResponse) => {
          this.handleError('Failed to load user records.', error);
        }
      });
  }

  /**
   * Toggles the active status of a user (Ban / Unban).
   */
  toggleUserStatus(targetUser: User): void {
    if (this.isSelf(targetUser.id)) return;

    this.updatingUserId.set(targetUser.id);
    this.errorMessage.set(null);
    const newStatus = !targetUser.isActive;

    this.adminService.updateUserStatus(targetUser.id, newStatus)
      .pipe(finalize(() => this.updatingUserId.set(null)))
      .subscribe({
        next: (response) => this.updateLocalUserRecord(response.data),
        error: (error: HttpErrorResponse) => {
          this.handleError(`Failed to update status for ${targetUser.email}.`, error);
        }
      });
  }

  /**
   * Promotes a USER to ADMIN, or demotes an ADMIN to USER.
   */
  toggleUserRole(targetUser: User): void {
    if (this.isSelf(targetUser.id)) return;

    this.updatingUserId.set(targetUser.id);
    this.errorMessage.set(null);
    const newRole = targetUser.role === 'ADMIN' ? 'USER' : 'ADMIN';

    this.adminService.updateUserRole(targetUser.id, newRole)
      .pipe(finalize(() => this.updatingUserId.set(null)))
      .subscribe({
        next: (response) => this.updateLocalUserRecord(response.data),
        error: (error: HttpErrorResponse) => {
          this.handleError(`Failed to update role for ${targetUser.email}.`, error);
        }
      });
  }

  /**
   * Paginator control handlers.
   */
  goToNextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.loadUsers(this.currentPage() + 1);
    }
  }

  goToPreviousPage(): void {
    if (this.currentPage() > 0) {
      this.loadUsers(this.currentPage() - 1);
    }
  }

  /**
   * Utility check to prevent the current admin from locking themselves out.
   */
  isSelf(userId: string): boolean {
    return this.currentAdminId() === userId;
  }

  /**
   * In-place mutation of the specific user record within the Signal array 
   * to prevent a full-page re-render.
   */
  private updateLocalUserRecord(updatedUser: User): void {
    this.users.update(currentUsers =>
      currentUsers.map(u => u.id === updatedUser.id ? updatedUser : u)
    );
  }

  private handleError(contextMessage: string, error: HttpErrorResponse): void {
    console.error(contextMessage, error);
    const backendMessage = error.error?.message || 'An unexpected network error occurred.';
    this.errorMessage.set(`${contextMessage} ${backendMessage}`);
  }
}
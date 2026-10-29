import { Component, inject, OnInit, signal, computed, HostListener, DestroyRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { AdminService } from '../../../core/services/admin.service';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';
import { NeuButtonComponent } from '../../../shared/ui/neu-button/neu-button.component';

import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, finalize } from 'rxjs/operators';

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
  private readonly router = inject(Router); // NEW: Inject Router for modal dismissal
  private readonly destroyRef = inject(DestroyRef);

  searchQuery = signal<string>('');
  private readonly searchSubject = new Subject<string>();

  // --- Core State ---
  currentAdminId = signal<string | null>(null);
  users = signal<User[]>([]);

  // --- Pagination State ---
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  // --- Computed Pagination Range (e.g., Showing 1–10 of 15) ---
  rangeStart = computed(() =>
    this.totalElements() === 0 ? 0 : this.currentPage() * this.pageSize() + 1
  );
  rangeEnd = computed(() =>
    Math.min((this.currentPage() + 1) * this.pageSize(), this.totalElements())
  );

  // --- UI Loading & Error State ---
  isPageLoading = signal<boolean>(true);
  updatingUserId = signal<string | null>(null);
  errorMessage = signal<string | null>(null);
  cascadeFlip = signal<boolean>(false);

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      this.currentPage.set(0);
      this.loadUsers(0);
    });

    this.initializeAdminContext();
  }

  onSearchInput(value: string): void {
    this.searchQuery.set(value);
    this.searchSubject.next(value);
  }

  clearSearch(): void {
    if (!this.searchQuery()) return;
    this.searchQuery.set('');
    this.searchSubject.next('');
  }

  // NEW: Enterprise UX - Close modal on Escape key press
  @HostListener('window:keydown.escape')
  onEscKey(): void {
    this.closeModal();
  }

  // NEW: Routing logic to exit the portal
  closeModal(): void {
    this.router.navigate(['/overview']);
  }

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

  loadUsers(pageIndex: number): void {
    this.isPageLoading.set(true);
    this.errorMessage.set(null);

    this.adminService.getAllUsers(pageIndex, this.pageSize(), this.searchQuery())
      .pipe(finalize(() => this.isPageLoading.set(false)))
      .subscribe({
        next: (response) => {
          // If unbanning the last user on Page 2 leaves Page 2 empty, step back to Page 1
          if (response.data.content.length === 0 && pageIndex > 0) {
            this.loadUsers(pageIndex - 1);
            return;
          }
          this.users.set(response.data.content);
          this.cascadeFlip.update(v => !v); // <-- Re-triggers staggered CSS row animation
          this.currentPage.set(response.data.number);
          this.totalElements.set(response.data.totalElements);
          this.totalPages.set(response.data.totalPages);
        },
        error: (error: HttpErrorResponse) => {
          this.handleError('Failed to load user records.', error);
        }
      });
  }

  toggleUserStatus(targetUser: User): void {
    if (this.isSelf(targetUser.id)) return;

    this.updatingUserId.set(targetUser.id);
    this.errorMessage.set(null);
    const newStatus = !targetUser.isActive;

    this.adminService.updateUserStatus(targetUser.id, newStatus)
      .pipe(finalize(() => this.updatingUserId.set(null)))
      .subscribe({
        // Re-fetch current page so unbanned users immediately jump to Page 1
        next: () => this.loadUsers(this.currentPage()),
        error: (error: HttpErrorResponse) => {
          this.handleError(`Failed to update status for ${targetUser.email}.`, error);
        }
      });
  }

  toggleUserRole(targetUser: User): void {
    if (this.isSelf(targetUser.id)) return;

    this.updatingUserId.set(targetUser.id);
    this.errorMessage.set(null);
    const newRole = targetUser.role === 'ADMIN' ? 'USER' : 'ADMIN';

    this.adminService.updateUserRole(targetUser.id, newRole)
      .pipe(finalize(() => this.updatingUserId.set(null)))
      .subscribe({
        // Re-fetch current page so promoted/demoted users jump to their sorted page
        next: () => this.loadUsers(this.currentPage()),
        error: (error: HttpErrorResponse) => {
          this.handleError(`Failed to update role for ${targetUser.email}.`, error);
        }
      });
  }

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

  isSelf(userId: string): boolean {
    return this.currentAdminId() === userId;
  }

  private handleError(contextMessage: string, error: HttpErrorResponse): void {
    console.error(contextMessage, error);
    const backendMessage = error.error?.message || 'An unexpected network error occurred.';
    this.errorMessage.set(`${contextMessage} ${backendMessage}`);
  }
}
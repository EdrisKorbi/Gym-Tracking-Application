import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Page } from '../models/page.model';
import { User } from '../models/user.model';
import { UserRoleUpdateDTO } from '../models/user-role-update.dto';
import { UserStatusUpdateDTO } from '../models/user-status-update.dto';


@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/admin/users`;

    /**
     * GET /api/v1/admin/users
     * Retrieves a paginated list of users.
     */
    getAllUsers(
        page: number = 0,
        size: number = 10,
        search: string = ''
    ): Observable<ApiResponse<Page<User>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .append('sort', 'role,asc')       // 1st Priority: ADMIN before USER
            .append('sort', 'isActive,desc')  // 2nd Priority: Active before Suspended
            .append('sort', 'createdAt,desc'); // 3rd Priority: Newest first

        const trimmed = search.trim();
        if (trimmed) {
            params = params.set('search', trimmed);
        }

        return this.http.get<ApiResponse<Page<User>>>(this.apiUrl, { params });
    }

    /**
     * PATCH /api/v1/admin/users/{id}/status
     * Activates or deactivates (bans) a user.
     */
    updateUserStatus(id: string, isActive: boolean): Observable<ApiResponse<User>> {
        const payload: UserStatusUpdateDTO = { isActive };
        return this.http.patch<ApiResponse<User>>(`${this.apiUrl}/${id}/status`, payload);
    }

    /**
     * PATCH /api/v1/admin/users/{id}/role
     * Promotes or demotes a user's role.
     */
    updateUserRole(id: string, role: 'USER' | 'ADMIN'): Observable<ApiResponse<User>> {
        const payload: UserRoleUpdateDTO = { role };
        return this.http.patch<ApiResponse<User>>(`${this.apiUrl}/${id}/role`, payload);
    }
}
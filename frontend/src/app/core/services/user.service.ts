import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

import { ApiResponse } from '../models/api-response.model';
import { User } from '../models/user.model';
import { UserUpdateDTO } from '../models/user-update.dto';
import { PasswordUpdateDTO } from '../models/password-update.dto';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly http = inject(HttpClient);

    // Maps strictly to your backend UserController @RequestMapping("/api/v1/users") + @GetMapping("/me")
    private readonly apiUrl = `${environment.apiUrl}/users/me`;

    getProfile(): Observable<ApiResponse<User>> {
        return this.http.get<ApiResponse<User>>(this.apiUrl);
    }

    updateProfile(data: UserUpdateDTO): Observable<ApiResponse<User>> {
        return this.http.put<ApiResponse<User>>(this.apiUrl, data);
    }

    updatePassword(data: PasswordUpdateDTO): Observable<ApiResponse<void>> {
        return this.http.put<ApiResponse<void>>(`${this.apiUrl}/password`, data);
    }

    deactivateAccount(): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(this.apiUrl);
    }
}
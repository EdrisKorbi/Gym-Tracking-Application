import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { User } from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private http = inject(HttpClient);

    // The base URL strictly mapped to our Spring Boot self-service endpoints
    private readonly API_URL = 'http://localhost:8080/api/v1/users/me';

    // 1. GET FRESH PROFILE
    getProfile(): Observable<ApiResponse<User>> {
        return this.http.get<ApiResponse<User>>(this.API_URL);
    }

    // 2. UPDATE PROFILE (First Name, Last Name)
    updateProfile(data: { firstName: string; lastName: string }): Observable<ApiResponse<User>> {
        return this.http.put<ApiResponse<User>>(this.API_URL, data);
    }

    // 3. UPDATE PASSWORD
    updatePassword(data: any): Observable<ApiResponse<void>> {
        return this.http.put<ApiResponse<void>>(`${this.API_URL}/password`, data);
    }

    // 4. SOFT DELETE ACCOUNT
    deactivateAccount(): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(this.API_URL);
    }
}
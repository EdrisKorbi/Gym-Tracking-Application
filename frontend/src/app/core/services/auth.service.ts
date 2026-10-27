import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

import { ApiResponse } from '../models/api-response.model';
import { User, AuthResponse, TokenResponse, RegisterRequest } from '../models/user.model';

@Injectable({
    providedIn: 'root' // Makes this a singleton available everywhere
})
export class AuthService {
    // Modern Angular dependency injection
    private http = inject(HttpClient);
    private router = inject(Router);

    private apiUrl = `${environment.apiUrl}/users`;

    // The reactive state holding our currently logged-in user. Null means guest.
    currentUser = signal<User | null>(null);

    constructor() {
        this.loadUserFromStorage();
    }

    // 1. REGISTER
    register(data: RegisterRequest): Observable<ApiResponse<User>> {
        // Notice we do NOT use tap() here to save tokens. 
        // In our backend design, registration does not auto-login the user.
        // They must explicitly log in after successfully registering.
        return this.http.post<ApiResponse<User>>(`${this.apiUrl}/register`, data);
    }

    // 1. LOGIN
    login(credentials: any): Observable<ApiResponse<AuthResponse>> {
        return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, credentials).pipe(
            // tap() allows us to run side-effects (like saving tokens) without changing the data stream
            tap(response => {
                if (response.success && response.data) {
                    this.setSession(response.data.token, response.data.refreshToken);
                }
            })
        );
    }

    // 2. REFRESH TOKEN
    refreshToken(refreshToken: string): Observable<ApiResponse<TokenResponse>> {
        return this.http.post<ApiResponse<TokenResponse>>(
            `${this.apiUrl}/refresh`,
            { refreshToken }
        ).pipe(
            tap(response => {
                if (response.success && response.data) {
                    // Update local storage with the fresh tokens
                    localStorage.setItem('token', response.data.token);
                    localStorage.setItem('refreshToken', response.data.refreshToken);
                }
            })
        );
    }

    // 3. LOGOUT
    logout(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        this.currentUser.set(null);
        this.router.navigate(['/login']);
    }

    // 4. SYNC PROFILE (Updates global user state and storage after /users/me fetch)
    syncProfile(user: User): void {
        localStorage.setItem('user', JSON.stringify(user));
        this.currentUser.set(user);
    }

    // Helpers to get tokens for our upcoming Interceptor
    getAccessToken(): string | null {
        return localStorage.getItem('token');
    }

    getRefreshToken(): string | null {
        return localStorage.getItem('refreshToken');
    }

    // Private helper to save tokens returned by the login endpoint
    private setSession(token: string, refreshToken: string): void {
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);
    }

    // Runs once when the app starts up to check if the user is already logged in
    private loadUserFromStorage(): void {
        const storedUser = localStorage.getItem('user');

        // Check if it exists AND ensure it's not the literal string "undefined"
        if (storedUser && storedUser !== 'undefined') {
            try {
                const user = JSON.parse(storedUser);
                this.currentUser.set(user);
            } catch (error) {
                // If the JSON is corrupted, catch the error, wipe the bad data, and log the user out silently
                console.error('Corrupted session data found. Clearing storage.');
                localStorage.removeItem('user');
                this.currentUser.set(null);
            }
        }
    }
}
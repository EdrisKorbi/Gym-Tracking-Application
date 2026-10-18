// Strict union type to enforce exact role matching
export type Role = 'USER' | 'ADMIN';

export interface User {
    id: string; // UUID from PostgreSQL
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
    isActive: boolean;
    createdAt: string;
}

export interface AuthResponse {
    token: string;        // 15-minute JWT Access Token
    refreshToken: string; // 7-day Refresh Token
    user: User;           // The logged-in user's profile details
}

export interface TokenResponse {
    token: string;
    refreshToken: string;
}

export interface RegisterRequest {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
}
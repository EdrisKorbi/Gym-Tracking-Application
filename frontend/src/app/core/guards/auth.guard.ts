import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    // If a token exists in storage, allow routing to the dashboard.
    // The Dashboard component will use this token to fetch the actual user profile.
    if (authService.getAccessToken()) {
        return true;
    }

    router.navigate(['/login']);
    return false;
};
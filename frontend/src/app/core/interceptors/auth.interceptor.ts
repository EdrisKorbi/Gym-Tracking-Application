import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandlerFn,
    HttpInterceptorFn,
    HttpRequest
} from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, finalize, switchMap, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

// Mutex lock: tracks whether a token refresh network call is currently in flight
let isRefreshing = false;

// Queue ("waiting room"): holds the latest refreshed JWT for concurrent requests waiting on a refresh
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const authService = inject(AuthService);

    // 1. Identify public authentication endpoints that should never trigger a token refresh loop
    const isAuthEndpoint =
        req.url.includes('/api/v1/users/login') ||
        req.url.includes('/api/v1/users/register') ||
        req.url.includes('/api/v1/users/refresh');

    // 2. Attach the current JWT Access Token if we have one and this is not an auth endpoint
    const accessToken = authService.getAccessToken();
    const authReq =
        accessToken && !isAuthEndpoint ? addTokenHeader(req, accessToken) : req;

    // 3. Send the request and listen for 401/403 token expiration errors
    return next(authReq).pipe(
        catchError((error: unknown) => {
            if (
                error instanceof HttpErrorResponse &&
                (error.status === 401 || error.status === 403) &&
                !isAuthEndpoint
            ) {
                return handleTokenExpiration(authReq, next, authService);
            }

            // For all other errors (e.g., 400 Bad Request, 404 Not Found, 409 Conflict), pass them through
            return throwError(() => error);
        })
    );
};

/**
 * Clones the immutable HttpRequest and attaches the Authorization Bearer header.
 */
function addTokenHeader(
    request: HttpRequest<unknown>,
    token: string
): HttpRequest<unknown> {
    return request.clone({
        setHeaders: {
            Authorization: `Bearer ${token}`
        }
    });
}

/**
 * Coordinates silent token refresh and queues concurrent failed requests.
 */
function handleTokenExpiration(
    request: HttpRequest<unknown>,
    next: HttpHandlerFn,
    authService: AuthService
): Observable<HttpEvent<unknown>> {
    // CASE A: No refresh is running yet — this request takes responsibility for refreshing
    if (!isRefreshing) {
        isRefreshing = true;
        // Clear the scoreboard so concurrent requests wait until the new token arrives
        refreshTokenSubject.next(null);

        const refreshToken = authService.getRefreshToken();

        // If no refresh token exists in storage, force a clean logout immediately
        if (!refreshToken) {
            isRefreshing = false;
            authService.logout();
            return throwError(() => new Error('Refresh token missing. Session terminated.'));
        }

        return authService.refreshToken(refreshToken).pipe(
            switchMap((response) => {
                const newAccessToken = response.data.token;
                // Broadcast the new token to all waiting requests in the BehaviorSubject queue
                refreshTokenSubject.next(newAccessToken);
                // Retry the original failed request with the brand-new token
                return next(addTokenHeader(request, newAccessToken));
            }),
            catchError((refreshError: unknown) => {
                // If the 7-day refresh token is expired or revoked (e.g. banned user), wipe session and logout
                authService.logout();
                return throwError(() => refreshError);
            }),
            finalize(() => {
                // Always release the lock when the refresh request finishes (whether it succeeded or failed)
                isRefreshing = false;
            })
        );
    }

    // CASE B: A refresh is ALREADY in progress — wait in the queue for the new token
    return refreshTokenSubject.pipe(
        // Wait until the scoreboard value is no longer null (using a strict TypeScript type guard)
        filter((token): token is string => token !== null),
        // Take only the very first valid token emitted, then automatically unsubscribe from the queue
        take(1),
        // Replay this paused request with the newly arrived token
        switchMap((token) => next(addTokenHeader(request, token)))
    );
}
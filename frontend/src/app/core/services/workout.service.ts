// src/app/core/services/workout.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { Page } from '../models/page.model';
import { WorkoutResponse, WorkoutRequest } from '../models/workout.model';

@Injectable({
    providedIn: 'root'
})
export class WorkoutService {
    private http = inject(HttpClient);
    private readonly API_URL = 'http://localhost:8080/api/v1/workouts';

    // GET /api/v1/workouts with Pagination and Sorting
    getAllWorkouts(page: number = 0, size: number = 10, sort: string = 'date,desc'): Observable<ApiResponse<Page<WorkoutResponse>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', sort);

        return this.http.get<ApiResponse<Page<WorkoutResponse>>>(this.API_URL, { params });
    }

    // GET /api/v1/workouts/{id}
    getWorkoutById(id: string): Observable<ApiResponse<WorkoutResponse>> {
        return this.http.get<ApiResponse<WorkoutResponse>>(`${this.API_URL}/${id}`);
    }

    // POST /api/v1/workouts
    logWorkout(workout: WorkoutRequest): Observable<ApiResponse<WorkoutResponse>> {
        return this.http.post<ApiResponse<WorkoutResponse>>(this.API_URL, workout);
    }

    // PUT /api/v1/workouts/{id}
    updateWorkout(id: string, workout: WorkoutRequest): Observable<ApiResponse<WorkoutResponse>> {
        return this.http.put<ApiResponse<WorkoutResponse>>(`${this.API_URL}/${id}`, workout);
    }

    // DELETE /api/v1/workouts/{id}
    deleteWorkout(id: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`);
    }
}
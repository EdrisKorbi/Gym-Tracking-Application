export interface ApiResponse<T> {
    timestamp: string; // Stored as an ISO-8601 string from backend LocalDateTime
    success: boolean;
    message: string;
    data: T;           // The generic payload (User, Page<Workout>, etc.)
}
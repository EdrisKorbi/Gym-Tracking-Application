// Maps to com.gymapp.backend.enums.WorkoutStatus
export type WorkoutStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED';

// Maps exactly to ExerciseResponseDTO.java
export interface ExerciseResponse {
    id: string; // UUID mapped to TS string
    name: string;
    sets: number;
    reps: number;
    weight: number;
    exerciseOrder: number; // Guarantees list sequencing
}

// Maps exactly to WorkoutResponseDTO.java
export interface WorkoutResponse {
    id: string;
    name: string;
    date: string; // LocalDateTime mapped to ISO-8601 string
    routineId: string | null; // Nullable if not tied to a blueprint
    phaseId: string | null;
    status: WorkoutStatus;
    exercises: ExerciseResponse[];
}

// Maps exactly to ExerciseRequestDTO.java
export interface ExerciseRequest {
    id?: string; // Optional for new exercises during a PUT
    name: string;
    sets: number;
    reps: number;
    weight: number;
    exerciseOrder: number;
}

// Maps exactly to WorkoutRequestDTO.java
export interface WorkoutRequest {
    name: string;
    date: string;
    routineId: string | null;
    phaseId: string | null;
    status: WorkoutStatus;
    exercises: ExerciseRequest[];
}
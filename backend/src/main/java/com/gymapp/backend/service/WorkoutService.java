package com.gymapp.backend.service;

import com.gymapp.backend.dto.*;
import com.gymapp.backend.entity.Exercise;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.entity.Workout;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    // Notice we removed ExerciseRepository entirely! The Workout entity handles it now.
    public WorkoutService(WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutResponseDTO logWorkout(String email, WorkoutRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(requestDTO.getName());
        workout.setDate(requestDTO.getDate());

        // Map new Phase 3 fields
        workout.setRoutineId(requestDTO.getRoutineId());
        workout.setPhaseId(requestDTO.getPhaseId());
        workout.setStatus(requestDTO.getStatus());

        // Add exercises directly to the Workout object
        for (ExerciseRequestDTO exDto : requestDTO.getExercises()) {
            Exercise exercise = new Exercise();
            exercise.setWorkout(workout);
            exercise.setName(exDto.getName());
            exercise.setSets(exDto.getSets());
            exercise.setReps(exDto.getReps());
            exercise.setWeight(exDto.getWeight());
            exercise.setExerciseOrder(exDto.getExerciseOrder()); // Map new list sequence

            workout.getExercises().add(exercise);
        }

        // Saving the workout cascades and saves all exercises automatically
        Workout savedWorkout = workoutRepository.save(workout);

        return mapToResponseDTO(savedWorkout);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponseDTO> getAllWorkouts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Workout> workouts = workoutRepository.findByUserId(user.getId());
        return workouts.stream().map(this::mapToResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponseDTO getWorkoutById(String email, UUID workoutId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this workout");
        }

        return mapToResponseDTO(workout);
    }

    @Transactional
    public WorkoutResponseDTO updateWorkout(String email, UUID workoutId, WorkoutRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this workout");
        }

        // 1. Update parent workout fields
        workout.setName(requestDTO.getName());
        workout.setDate(requestDTO.getDate());
        workout.setRoutineId(requestDTO.getRoutineId());
        workout.setPhaseId(requestDTO.getPhaseId());
        workout.setStatus(requestDTO.getStatus());

        // 2. Fetch existing exercises directly from the entity (No ExerciseRepository needed)
        Map<UUID, Exercise> existingExerciseMap = workout.getExercises().stream()
                .collect(Collectors.toMap(Exercise::getId, ex -> ex));

        // Clear the current list so we can rebuild it purely from the incoming request
        workout.getExercises().clear();

        // 3. Diff and Merge logic
        for (ExerciseRequestDTO exDto : requestDTO.getExercises()) {
            if (exDto.getId() != null && existingExerciseMap.containsKey(exDto.getId())) {
                // UPDATE: Exercise exists. Update values and put it back in the list.
                Exercise existingEx = existingExerciseMap.get(exDto.getId());
                existingEx.setName(exDto.getName());
                existingEx.setSets(exDto.getSets());
                existingEx.setReps(exDto.getReps());
                existingEx.setWeight(exDto.getWeight());
                existingEx.setExerciseOrder(exDto.getExerciseOrder());

                workout.getExercises().add(existingEx);
                existingExerciseMap.remove(exDto.getId());
            } else {
                // INSERT: Brand new exercise added during edit.
                Exercise newEx = new Exercise();
                newEx.setWorkout(workout);
                newEx.setName(exDto.getName());
                newEx.setSets(exDto.getSets());
                newEx.setReps(exDto.getReps());
                newEx.setWeight(exDto.getWeight());
                newEx.setExerciseOrder(exDto.getExerciseOrder());

                workout.getExercises().add(newEx);
            }
        }
        // Because of orphanRemoval = true, anything left in existingExerciseMap
        // that wasn't added back to workout.getExercises() will be deleted from the DB automatically.

        // Save the parent, Hibernate handles the rest.
        Workout updatedWorkout = workoutRepository.save(workout);

        return mapToResponseDTO(updatedWorkout);
    }

    @Transactional
    public void deleteWorkout(String email, UUID workoutId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this workout");
        }

        // Because of cascade = CascadeType.ALL, deleting the workout automatically deletes its exercises
        workoutRepository.delete(workout);
    }

    // Helper method to keep DTO mapping clean and centralized
    private WorkoutResponseDTO mapToResponseDTO(Workout workout) {
        WorkoutResponseDTO response = new WorkoutResponseDTO();
        response.setId(workout.getId());
        response.setName(workout.getName());
        response.setDate(workout.getDate());
        response.setRoutineId(workout.getRoutineId());
        response.setPhaseId(workout.getPhaseId());
        response.setStatus(workout.getStatus());

        List<ExerciseResponseDTO> exerciseDTOs = workout.getExercises().stream().map(ex -> {
            ExerciseResponseDTO dto = new ExerciseResponseDTO();
            dto.setId(ex.getId());
            dto.setName(ex.getName());
            dto.setSets(ex.getSets());
            dto.setReps(ex.getReps());
            dto.setWeight(ex.getWeight());
            dto.setExerciseOrder(ex.getExerciseOrder());
            return dto;
        }).toList();

        response.setExercises(exerciseDTOs);
        return response;
    }
}
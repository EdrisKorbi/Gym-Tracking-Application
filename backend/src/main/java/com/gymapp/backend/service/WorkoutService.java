package com.gymapp.backend.service;

import com.gymapp.backend.dto.*;
import com.gymapp.backend.entity.Exercise;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.entity.Workout;
import com.gymapp.backend.exception.AccessDeniedException;
import com.gymapp.backend.exception.ResourceNotFoundException;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WorkoutRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public WorkoutService(WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutResponseDTO logWorkout(UUID userId, WorkoutRequestDTO requestDTO) {
        User userProxy = userRepository.getReferenceById(userId);

        Workout workout = new Workout();
        workout.setUser(userProxy);
        workout.setName(requestDTO.getName());
        workout.setDate(requestDTO.getDate());
        workout.setRoutineId(requestDTO.getRoutineId());
        workout.setPhaseId(requestDTO.getPhaseId());
        workout.setStatus(requestDTO.getStatus());

        for (ExerciseRequestDTO exDto : requestDTO.getExercises()) {
            Exercise exercise = new Exercise();
            exercise.setWorkout(workout);
            exercise.setName(exDto.getName());
            exercise.setSets(exDto.getSets());
            exercise.setReps(exDto.getReps());
            exercise.setWeight(exDto.getWeight());
            exercise.setExerciseOrder(exDto.getExerciseOrder());

            workout.getExercises().add(exercise);
        }

        Workout savedWorkout = workoutRepository.save(workout);
        return mapToResponseDTO(savedWorkout);
    }

    // NEW: Now returns Page instead of List
    @Transactional(readOnly = true)
    public Page<WorkoutResponseDTO> getAllWorkouts(UUID userId, Pageable pageable) {
        Page<Workout> workouts = workoutRepository.findByUserId(userId, pageable);
        return workouts.map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public WorkoutResponseDTO getWorkoutById(UUID userId, UUID workoutId) {
        Workout workout = workoutRepository.findByIdWithExercises(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: You do not own this workout");
        }

        return mapToResponseDTO(workout);
    }

    @Transactional
    public WorkoutResponseDTO updateWorkout(UUID userId, UUID workoutId, WorkoutRequestDTO requestDTO) {
        // PERFORMANCE WIN: Fetches Workout and Exercises in 1 query before diff/merge
        Workout workout = workoutRepository.findByIdWithExercises(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: You do not own this workout");
        }

        workout.setName(requestDTO.getName());
        workout.setDate(requestDTO.getDate());
        workout.setRoutineId(requestDTO.getRoutineId());
        workout.setPhaseId(requestDTO.getPhaseId());
        workout.setStatus(requestDTO.getStatus());

        Map<UUID, Exercise> existingExerciseMap = workout.getExercises().stream()
                .collect(Collectors.toMap(Exercise::getId, ex -> ex));

        workout.getExercises().clear();

        for (ExerciseRequestDTO exDto : requestDTO.getExercises()) {
            if (exDto.getId() != null && existingExerciseMap.containsKey(exDto.getId())) {
                Exercise existingEx = existingExerciseMap.get(exDto.getId());
                existingEx.setName(exDto.getName());
                existingEx.setSets(exDto.getSets());
                existingEx.setReps(exDto.getReps());
                existingEx.setWeight(exDto.getWeight());
                existingEx.setExerciseOrder(exDto.getExerciseOrder());

                workout.getExercises().add(existingEx);
                existingExerciseMap.remove(exDto.getId());
            } else {
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

        Workout updatedWorkout = workoutRepository.save(workout);
        return mapToResponseDTO(updatedWorkout);
    }

    @Transactional
    public void deleteWorkout(UUID userId, UUID workoutId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: You do not own this workout");
        }

        workoutRepository.delete(workout);
    }

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
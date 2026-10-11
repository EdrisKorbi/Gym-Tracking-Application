package com.gymapp.backend.service;

import com.gymapp.backend.dto.*;
import com.gymapp.backend.entity.Exercise;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.entity.Workout;
import com.gymapp.backend.repository.ExerciseRepository;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public WorkoutService(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutResponseDTO logWorkout(String email, WorkoutRequestDTO requestDTO) {
        // 1. Find the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Create and save the Workout
        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(requestDTO.getName());
        workout.setDate(requestDTO.getDate());

        Workout savedWorkout = workoutRepository.save(workout);

        // 3. Create and save new exercises using Streams
        List<Exercise> newExercises = requestDTO.getExercises().stream().map(exDto -> {
            Exercise exercise = new Exercise();
            exercise.setWorkout(savedWorkout); // Link to the parent workout
            exercise.setName(exDto.getName());
            exercise.setSets(exDto.getSets());
            exercise.setReps(exDto.getReps());
            exercise.setWeight(exDto.getWeight());
            return exercise;
        }).toList();

        List<Exercise> savedExercises = exerciseRepository.saveAll(newExercises);

        // 4. Map saved entities to Response DTOs using Streams
        List<ExerciseResponseDTO> exerciseResponseList = savedExercises.stream().map(ex -> {
            ExerciseResponseDTO dto = new ExerciseResponseDTO();
            dto.setId(ex.getId());
            dto.setName(ex.getName());
            dto.setSets(ex.getSets());
            dto.setReps(ex.getReps());
            dto.setWeight(ex.getWeight());
            return dto;
        }).toList();

        // 5. Construct final response
        WorkoutResponseDTO finalResponse = new WorkoutResponseDTO();
        finalResponse.setId(savedWorkout.getId());
        finalResponse.setName(savedWorkout.getName());
        finalResponse.setDate(savedWorkout.getDate());
        finalResponse.setExercises(exerciseResponseList);

        return finalResponse;
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

    private WorkoutResponseDTO mapToResponseDTO(Workout workout) {
        WorkoutResponseDTO response = new WorkoutResponseDTO();
        response.setId(workout.getId());
        response.setName(workout.getName());
        response.setDate(workout.getDate());

        List<Exercise> exercises = exerciseRepository.findByWorkoutId(workout.getId());

        List<ExerciseResponseDTO> exerciseDTOs = exercises.stream().map(ex -> {
            ExerciseResponseDTO dto = new ExerciseResponseDTO();
            dto.setId(ex.getId());
            dto.setName(ex.getName());
            dto.setSets(ex.getSets());
            dto.setReps(ex.getReps());
            dto.setWeight(ex.getWeight());
            return dto;
        }).toList();

        response.setExercises(exerciseDTOs);
        return response;
    }

    @Transactional
    public WorkoutResponseDTO updateWorkout(String email, UUID workoutId, WorkoutRequestDTO requestDTO) {
        // 1. Verify user and workout ownership
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this workout");
        }

        // 2. Update parent workout fields
        workout.setName(requestDTO.getName());
        workout.setDate(requestDTO.getDate());
        Workout updatedWorkout = workoutRepository.save(workout);

        // 3. Fetch existing exercises and convert them into a Map for lightning-fast lookups
        List<Exercise> existingExercises = exerciseRepository.findByWorkoutId(workout.getId());
        Map<UUID, Exercise> existingExerciseMap = existingExercises.stream()
                .collect(Collectors.toMap(Exercise::getId, ex -> ex));

        List<Exercise> exercisesToSave = new ArrayList<>();

        // 4. Diff and Merge logic
        for (ExerciseRequestDTO exDto : requestDTO.getExercises()) {
            if (exDto.getId() != null && existingExerciseMap.containsKey(exDto.getId())) {
                // UPDATE: The exercise exists in the database. Update its values in-place.
                Exercise existingEx = existingExerciseMap.get(exDto.getId());
                existingEx.setName(exDto.getName());
                existingEx.setSets(exDto.getSets());
                existingEx.setReps(exDto.getReps());
                existingEx.setWeight(exDto.getWeight());
                exercisesToSave.add(existingEx);

                // Remove it from the map so we know it was processed
                existingExerciseMap.remove(exDto.getId());
            } else {
                // INSERT: No ID provided (or invalid ID). This is a brand new exercise added during the edit.
                Exercise newEx = new Exercise();
                newEx.setWorkout(updatedWorkout);
                newEx.setName(exDto.getName());
                newEx.setSets(exDto.getSets());
                newEx.setReps(exDto.getReps());
                newEx.setWeight(exDto.getWeight());
                exercisesToSave.add(newEx);
            }
        }

        // 5. DELETE: Any exercises still left in the map were NOT in the incoming JSON, meaning the user deleted them.
        exerciseRepository.deleteAll(existingExerciseMap.values());

        // 6. Save Updates & Inserts
        List<Exercise> savedExercises = exerciseRepository.saveAll(exercisesToSave);

        // 7. Map to Response DTOs
        List<ExerciseResponseDTO> exerciseResponseList = savedExercises.stream().map(ex -> {
            ExerciseResponseDTO dto = new ExerciseResponseDTO();
            dto.setId(ex.getId());
            dto.setName(ex.getName());
            dto.setSets(ex.getSets());
            dto.setReps(ex.getReps());
            dto.setWeight(ex.getWeight());
            return dto;
        }).toList();

        WorkoutResponseDTO finalResponse = new WorkoutResponseDTO();
        finalResponse.setId(updatedWorkout.getId());
        finalResponse.setName(updatedWorkout.getName());
        finalResponse.setDate(updatedWorkout.getDate());
        finalResponse.setExercises(exerciseResponseList);

        return finalResponse;
    }

    @Transactional
    public void deleteWorkout(String email, UUID workoutId) {
        // 1. Verify user and workout ownership
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this workout");
        }

        // 2. Delete all linked exercises first to prevent Foreign Key constraint errors
        List<Exercise> exercises = exerciseRepository.findByWorkoutId(workout.getId());
        exerciseRepository.deleteAll(exercises);

        // 3. Delete the parent workout
        workoutRepository.delete(workout);
    }
}
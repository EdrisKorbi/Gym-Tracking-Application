package com.gymapp.backend.service;

import com.gymapp.backend.dto.WorkoutGenerationRequestDTO;
import com.gymapp.backend.entity.*;
import com.gymapp.backend.enums.WorkoutStatus;
import com.gymapp.backend.repository.ProgramRepository;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gymapp.backend.exception.ResourceNotFoundException;
import com.gymapp.backend.exception.AccessDeniedException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkoutGenerationService {

    private final ProgramRepository programRepository;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public WorkoutGenerationService(ProgramRepository programRepository,
                                    WorkoutRepository workoutRepository,
                                    UserRepository userRepository) {
        this.programRepository = programRepository;
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<Workout> generateWorkoutsFromProgram(UUID programId, UUID userId, WorkoutGenerationRequestDTO request) {

        // 1. THE PERFORMANCE WIN: Get a lightweight proxy reference. NO DATABASE QUERY RUNS HERE!
        User userProxy = userRepository.getReferenceById(userId);

        // 2. Fetch the Blueprint
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with ID: " + programId));

        // Security check: Ensure the user actually owns this program (or it's a template)
        if (!program.getUser().getId().equals(userId) && !program.getIsTemplate()) {
            throw new AccessDeniedException("You do not have permission to generate this program");
        }

        List<Workout> generatedWorkouts = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();
        List<DayOfWeek> preferredDays = request.getPreferredDays();

        // 3. Iterate through Macrocycle (Phases)
        for (ProgramPhase phase : program.getPhases()) {

            // 4. Iterate through Microcycle (Routines)
            for (Routine routine : phase.getRoutines()) {

                currentDate = calculateNextWorkoutDate(currentDate, preferredDays);

                // Instantiate the Reality Workout Log
                Workout workout = new Workout();
                workout.setUser(userProxy); // Attach the proxy user!
                workout.setRoutineId(routine.getId());
                workout.setPhaseId(phase.getId());
                workout.setName(routine.getName());
                workout.setDate(currentDate.atStartOfDay());
                workout.setStatus(WorkoutStatus.PLANNED);

                // 5. Map Blueprint targets to Reality logs
                for (RoutineExercise rx : routine.getRoutineExercises()) {
                    Exercise exercise = new Exercise();
                    exercise.setWorkout(workout);
                    exercise.setName(rx.getName());
                    exercise.setSets(rx.getTargetSets());
                    exercise.setReps(rx.getTargetReps());
                    exercise.setWeight(0.0);
                    exercise.setExerciseOrder(rx.getExerciseOrder());

                    workout.getExercises().add(exercise);
                }

                generatedWorkouts.add(workout);
                currentDate = currentDate.plusDays(1);
            }
        }

        // 6. Batch save all generated workouts
        return workoutRepository.saveAll(generatedWorkouts);
    }

    private LocalDate calculateNextWorkoutDate(LocalDate currentDate, List<DayOfWeek> preferredDays) {
        LocalDate nextDate = currentDate;
        while (!preferredDays.contains(nextDate.getDayOfWeek())) {
            nextDate = nextDate.plusDays(1);
        }
        return nextDate;
    }
}
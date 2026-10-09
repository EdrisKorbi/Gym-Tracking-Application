-- 1. Users Table (UUID primary key + authentication & profile fields)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Exercises Dictionary
CREATE TABLE exercises (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    target_muscle VARCHAR(50) NOT NULL
);

-- 3. Programs (Templates & Custom)
CREATE TABLE programs (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    is_template BOOLEAN DEFAULT FALSE
);

-- 4. Program Phases
CREATE TABLE program_phases (
    id SERIAL PRIMARY KEY,
    program_id INTEGER REFERENCES programs(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    phase_order INTEGER NOT NULL
);

-- 5. Workouts
CREATE TABLE workouts (
    id SERIAL PRIMARY KEY,
    phase_id INTEGER REFERENCES program_phases(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    day_number INTEGER NOT NULL
);

-- 6. Workout Exercises (Targets)
CREATE TABLE workout_exercises (
    id SERIAL PRIMARY KEY,
    workout_id INTEGER REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id INTEGER REFERENCES exercises(id) ON DELETE RESTRICT,
    target_sets INTEGER NOT NULL,
    target_reps VARCHAR(50) NOT NULL
);

-- 7. Workout Sessions (Actual Gym Visits)
CREATE TABLE workout_sessions (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    workout_id INTEGER REFERENCES workouts(id) ON DELETE SET NULL,
    session_date DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 8. Set Logs (Granular Tracking)
CREATE TABLE set_logs (
    id SERIAL PRIMARY KEY,
    session_id INTEGER REFERENCES workout_sessions(id) ON DELETE CASCADE,
    workout_exercise_id INTEGER REFERENCES workout_exercises(id) ON DELETE CASCADE,
    set_number INTEGER NOT NULL,
    weight NUMERIC(6,2) NOT NULL,
    reps INTEGER NOT NULL,
    rpe NUMERIC(3,1)
);

-- 9. Exercise Notes (For AI/RAG)
CREATE TABLE exercise_notes (
    id SERIAL PRIMARY KEY,
    session_id INTEGER REFERENCES workout_sessions(id) ON DELETE CASCADE,
    exercise_id INTEGER REFERENCES exercises(id) ON DELETE CASCADE,
    note_text TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 10. Performance Indexes
CREATE INDEX idx_programs_user_id ON programs(user_id);
CREATE INDEX idx_sessions_user_id ON workout_sessions(user_id);
CREATE INDEX idx_set_logs_session_id ON set_logs(session_id);
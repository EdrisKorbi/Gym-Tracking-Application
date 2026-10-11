-- 1. Users Table (Identity & Security)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==========================================
-- PHASE 3: PROGRAMMING & BLUEPRINTS
-- ==========================================

-- 2. Programs (Macrocycle: e.g., "12-Week Powerbuilding")
CREATE TABLE programs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_template BOOLEAN DEFAULT FALSE
);

-- 3. Program Phases (Mesocycle: e.g., "Month 1: Hypertrophy")
CREATE TABLE program_phases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id UUID REFERENCES programs(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    phase_order INTEGER NOT NULL
);

-- 4. Routines (Microcycle/Blueprint: e.g., "Push Day")
CREATE TABLE routines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phase_id UUID REFERENCES program_phases(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    day_number INTEGER NOT NULL
);

-- 5. Routine Exercises (The planned targets for the routine)
CREATE TABLE routine_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    routine_id UUID REFERENCES routines(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    target_sets INTEGER NOT NULL,
    target_reps INTEGER NOT NULL,
    exercise_order INTEGER DEFAULT 0 -- NEW: Guarantees list sequencing
);

-- ==========================================
-- PHASE 2: ACTUAL LOGGED DATA
-- ==========================================

-- 6. Workouts (The actual gym visit)
CREATE TABLE workouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    routine_id UUID REFERENCES routines(id) ON DELETE SET NULL, -- Populated if generated from a blueprint[cite: 6]
    phase_id UUID REFERENCES program_phases(id) ON DELETE SET NULL, -- Populated if custom but tied to a phase[cite: 6]
    name VARCHAR(100) NOT NULL,
    date TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED' -- NEW: Supports calendar scheduling and skipped days
);

-- 7. Exercises (The actual logged metrics)
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_id UUID REFERENCES workouts(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    sets INTEGER NOT NULL,
    reps INTEGER NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    exercise_order INTEGER DEFAULT 0 -- NEW: Guarantees list sequencing
);

-- ==========================================
-- PERFORMANCE INDEXES
-- ==========================================
CREATE INDEX idx_programs_user_id ON programs(user_id);
CREATE INDEX idx_phases_program_id ON program_phases(program_id);
CREATE INDEX idx_routines_phase_id ON routines(phase_id);
CREATE INDEX idx_workouts_user_id ON workouts(user_id);
CREATE INDEX idx_workouts_routine_id ON workouts(routine_id);
CREATE INDEX idx_workouts_phase_id ON workouts(phase_id); 
CREATE INDEX idx_exercises_workout_id ON exercises(workout_id);
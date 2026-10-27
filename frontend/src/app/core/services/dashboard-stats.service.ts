import { Injectable } from '@angular/core';
import { WorkoutResponse } from '../models/workout.model';
import { KpiCard } from '../models/kpi-card.model';

@Injectable({
    providedIn: 'root'
})
export class DashboardStatsService {

    public calculateKpis(workouts: WorkoutResponse[]): KpiCard[] {
        if (!workouts || workouts.length === 0) {
            return this.getEmptyKpis();
        }

        let totalSets = 0;
        let totalReps = 0;
        let completedWorkouts = 0;

        let minTime = new Date().getTime();
        let maxTime = new Date().getTime();

        if (workouts.length > 0) {
            minTime = Math.min(...workouts.map(w => new Date(w.date).getTime()));
            maxTime = Math.max(...workouts.map(w => new Date(w.date).getTime()));
        }

        // Arrays to hold historical data for our sparklines
        const setsTrend: number[] = [];
        const repsTrend: number[] = [];

        // The API returns workouts sorted date,desc (newest first).
        // We reverse a copy of the array so our charts read left-to-right (oldest to newest).
        const chronologicalWorkouts = [...workouts].reverse();

        chronologicalWorkouts.forEach(workout => {
            if (workout.status === 'COMPLETED') {
                completedWorkouts++;
            }

            let workoutSets = 0;
            let workoutReps = 0;

            workout.exercises?.forEach(ex => {
                totalSets += ex.sets;
                totalReps += (ex.sets * ex.reps);
                
                workoutSets += ex.sets;
                workoutReps += (ex.sets * ex.reps);
            });

            // Push this workout's total into our trend arrays
            setsTrend.push(workoutSets);
            repsTrend.push(workoutReps);
        });

        const avgSetsPerSession = completedWorkouts > 0 ? Math.round(totalSets / completedWorkouts) : 0;
        const avgRepsPerSession = completedWorkouts > 0 ? Math.round(totalReps / completedWorkouts) : 0;
        const completionRate = Math.round((completedWorkouts / workouts.length) * 100);

        const daysSpan = Math.max(1, (maxTime - minTime) / (1000 * 60 * 60 * 24));
        const weeksSpan = Math.max(1, daysSpan / 7);
        const weeklySets = Math.round(totalSets / weeksSpan);

        // A simple upward trending array for "Total Workouts"
        const workoutCountTrend = chronologicalWorkouts.map((_, index) => index + 1);

        return [
            {
                icon: '📊',
                title: 'Avg Sets / Session',
                value: `${avgSetsPerSession}`,
                trend: '--', 
                isPositive: true,
                highlighted: true,
                sparkline: this.generateSparkline(setsTrend)
            },
            {
                icon: '🏋️',
                title: 'Total Workouts',
                value: `${workouts.length}`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                sparkline: this.generateSparkline(workoutCountTrend)
            },
            {
                icon: '📅',
                title: 'Weekly Sets (Avg)',
                value: `${weeklySets}`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                sparkline: this.generateSparkline(setsTrend)
            },
            {
                icon: '📈',
                title: 'Avg Reps / Session',
                value: `${avgRepsPerSession.toLocaleString()}`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                sparkline: this.generateSparkline(repsTrend)
            },
            {
                icon: '🎯',
                title: 'Completion Rate',
                value: `${completionRate}%`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                // Using setsTrend here just to keep the line active and visually matching
                sparkline: this.generateSparkline(setsTrend) 
            }
        ];
    }

    /**
     * Translates an array of numeric data into an SVG path string.
     * Maps X from 2 to 78, and Y from 22 (bottom) to 4 (top).
     */
    private generateSparkline(data: number[]): string {
        if (!data || data.length === 0) return '';
        if (data.length === 1) return `M 2 13 L 78 13`; // Flat middle line if only 1 data point

        const maxVal = Math.max(...data);
        const minVal = Math.min(...data);
        const range = maxVal - minVal;

        // X-axis mapping parameters
        const startX = 2;
        const endX = 78;
        const width = endX - startX;
        const stepX = width / (data.length - 1);

        // Y-axis mapping parameters
        const minY = 4;  // Highest peak (closest to top)
        const maxY = 22; // Lowest trough (closest to bottom)
        const height = maxY - minY;

        let path = '';

        data.forEach((val, index) => {
            const currentX = Math.round(startX + (index * stepX));
            
            let currentY = maxY; // Default to bottom if flat
            if (range !== 0) {
                // Determine percentage of max (0.0 to 1.0)
                const percent = (val - minVal) / range;
                // Subtract from maxY because SVG Y-axis is inverted
                currentY = Math.round(maxY - (percent * height));
            }

            // Move for the first point, Line for all subsequent points
            if (index === 0) {
                path += `M ${currentX} ${currentY}`;
            } else {
                path += ` L ${currentX} ${currentY}`;
            }
        });

        return path;
    }

    private getEmptyKpis(): KpiCard[] {
        return [
            { icon: '📊', title: 'Avg Sets / Session', value: '0', trend: '-', isPositive: true, highlighted: true, sparkline: '' },
            { icon: '🏋️', title: 'Total Workouts', value: '0', trend: '-', isPositive: true, highlighted: false, sparkline: '' },
            { icon: '📅', title: 'Weekly Sets (Avg)', value: '0', trend: '-', isPositive: true, highlighted: false, sparkline: '' },
            { icon: '📈', title: 'Avg Reps / Session', value: '0', trend: '-', isPositive: true, highlighted: false, sparkline: '' },
            { icon: '🎯', title: 'Completion Rate', value: '0%', trend: '-', isPositive: true, highlighted: false, sparkline: '' }
        ];
    }
}
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

        workouts.forEach(workout => {
            if (workout.status === 'COMPLETED') {
                completedWorkouts++;
            }

            workout.exercises?.forEach(ex => {
                totalSets += ex.sets;
                totalReps += (ex.sets * ex.reps);
            });
        });

        const avgSetsPerSession = completedWorkouts > 0 ? Math.round(totalSets / completedWorkouts) : 0;
        const avgRepsPerSession = completedWorkouts > 0 ? Math.round(totalReps / completedWorkouts) : 0;
        const completionRate = Math.round((completedWorkouts / workouts.length) * 100);

        const daysSpan = Math.max(1, (maxTime - minTime) / (1000 * 60 * 60 * 24));
        const weeksSpan = Math.max(1, daysSpan / 7);
        const weeklySets = Math.round(totalSets / weeksSpan);

        return [
            {
                icon: '📊',
                title: 'Avg Sets / Session',
                value: `${avgSetsPerSession}`,
                trend: '--',
                isPositive: true,
                highlighted: true,
                sparkline: 'M2 22 L14 14 L26 19 L38 8 L50 16 L64 4 L78 12'
            },
            {
                icon: '🏋️',
                title: 'Total Workouts',
                value: `${workouts.length}`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                sparkline: 'M2 18 L16 22 L30 10 L44 15 L58 6 L78 9'
            },
            {
                icon: '📅',
                title: 'Weekly Sets (Avg)',
                value: `${weeklySets}`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                sparkline: 'M2 20 L18 16 L32 19 L48 11 L62 14 L78 5'
            },
            {
                icon: '📈',
                title: 'Avg Reps / Session',
                value: `${avgRepsPerSession.toLocaleString()}`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                sparkline: 'M2 19 L16 15 L32 21 L48 12 L64 14 L78 7'
            },
            {
                icon: '🎯',
                title: 'Completion Rate',
                value: `${completionRate}%`,
                trend: '--',
                isPositive: true,
                highlighted: false,
                sparkline: 'M2 16 L16 14 L30 18 L46 10 L62 12 L78 6'
            }
        ];
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
import { Component, inject, OnInit, signal } from '@angular/core';
import { WorkoutService } from '../../../core/services/workout.service';
import { DashboardStatsService } from '../../../core/services/dashboard-stats.service';
import { KpiCard } from '../../../core/models/kpi-card.model';
import { NeuStatCardComponent } from '../../../shared/ui/neu-stat-card/neu-stat-card.component';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [NeuStatCardComponent],
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {
  private workoutService = inject(WorkoutService);
  private dashboardStatsService = inject(DashboardStatsService);

  kpiCards = signal<KpiCard[]>([]);

  ngOnInit(): void {
    this.workoutService.getAllWorkouts(0, 100, 'date,desc').subscribe({
      next: (response) => {
        const workouts = response.data.content;
        const computedKpis = this.dashboardStatsService.calculateKpis(workouts);
        this.kpiCards.set(computedKpis);
      },
      error: (err) => console.error('Failed to load workouts', err)
    });
  }
}
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';
import { NeuButtonComponent } from '../../shared/ui/neu-button/neu-button.component';
import { NeuSegmentedComponent } from '../../shared/ui/neu-segmented/neu-segmented.component';
import { NeuStatCardComponent } from '../../shared/ui/neu-stat-card/neu-stat-card.component';

interface ChartPoint {
  x: number;
  y: number;
  label: string;
  kcal: string;
  prevKcal: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NeuButtonComponent,
    NeuSegmentedComponent,
    NeuStatCardComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private router = inject(Router);

  // Core State Signals
  currentUser = signal<User | null>(null);
  isLoading = signal<boolean>(true);
  isDarkMode = signal<boolean>(false);

  // Navigation & Filter Signals
  topTabs = ['Overview', 'Workouts', 'Nutrition'];
  selectedTopTab = signal<string>('Overview');

  rangeOptions = ['30 days', '90 days', 'This year'];
  selectedRange = signal<string>('30 days');

  chartGranularity = ['Day', 'Week', 'Month'];
  selectedGranularity = signal<string>('Day');

  activeChannel = signal<string>('All workouts');
  channels = [
    { name: 'All workouts', color: 'var(--accent-color)' },
    { name: 'Strength', color: '#2ecc71' },
    { name: 'Cardio', color: '#4a86e8' },
    { name: 'Mobility', color: '#f59f00' }
  ];

  compareMode = signal<boolean>(true);
  activeSideIcon = signal<string>('analytics');
  hoveredPointIndex = signal<number>(5);

  // Bounded Y datasets (strictly between Y=40 and Y=205 inside 0..230 SVG viewBox)
  private readonly datasets: Record<string, { current: number[]; prev: number[] }> = {
    'Day': {
      current: [185, 145, 165, 110, 140, 68, 125, 95, 52, 118, 74, 90],
      prev: [198, 175, 150, 168, 120, 102, 155, 130, 88, 148, 115, 135]
    },
    'Week': {
      current: [170, 125, 140, 85, 110, 60, 95, 72, 48, 88, 62, 55],
      prev: [190, 160, 155, 130, 145, 95, 120, 110, 85, 125, 98, 92]
    },
    'Month': {
      current: [195, 160, 135, 120, 95, 82, 70, 65, 58, 50, 45, 42],
      prev: [205, 185, 170, 155, 140, 125, 115, 105, 95, 88, 80, 75]
    }
  };

  readonly xLabels = ['Oct 1', 'Oct 4', 'Oct 7', 'Oct 10', 'Oct 13', 'Oct 16', 'Oct 19', 'Oct 22', 'Oct 25', 'Oct 27', 'Oct 29', 'Oct 30'];

  chartPoints = computed<ChartPoint[]>(() => {
    const gran = this.selectedGranularity();
    const tabOffset = this.topTabs.indexOf(this.selectedTopTab()) * 10;
    const rangeOffset = this.rangeOptions.indexOf(this.selectedRange()) * 6;
    const base = this.datasets[gran] || this.datasets['Day'];
    const count = base.current.length;

    return base.current.map((yVal, i) => {
      const x = Math.round((i / (count - 1)) * 860) + 20;
      const shift = (i % 2 === 0 ? tabOffset : -tabOffset) + (i % 3 === 0 ? -rangeOffset : rangeOffset);
      const adjustedY = Math.max(38, Math.min(205, yVal + shift));
      const prevY = base.prev[i];
      const kcalVal = Math.round(((220 - adjustedY) / 190) * 4800);
      const prevKcalVal = Math.round(((220 - prevY) / 190) * 4800);
      return {
        x,
        y: adjustedY,
        label: this.xLabels[i] || `Day ${i + 1}`,
        kcal: `${kcalVal.toLocaleString()} kcal`,
        prevKcal: `${prevKcalVal.toLocaleString()} kcal`
      };
    });
  });

  currentLinePath = computed(() => {
    const pts = this.chartPoints();
    return this.buildMonotoneCubicPath(pts.map(p => ({ x: p.x, y: p.y })), false);
  });

  currentAreaPath = computed(() => {
    const pts = this.chartPoints();
    return this.buildMonotoneCubicPath(pts.map(p => ({ x: p.x, y: p.y })), true);
  });

  prevLinePath = computed(() => {
    const gran = this.selectedGranularity();
    const base = this.datasets[gran] || this.datasets['Day'];
    const count = base.prev.length;
    const pts = base.prev.map((y, i) => ({
      x: Math.round((i / (count - 1)) * 860) + 20,
      y
    }));
    return this.buildMonotoneCubicPath(pts, false);
  });

  activePoint = computed(() => {
    const pts = this.chartPoints();
    return pts[this.hoveredPointIndex()] || pts[5];
  });

  // 6 KPI Cards Data
  kpiCards = [
    {
      icon: '🔥',
      title: 'Calories Burned',
      value: '25,523 kcal',
      trend: '14%',
      isPositive: true,
      highlighted: true,
      sparkline: 'M2 22 L14 14 L26 19 L38 8 L50 16 L64 4 L78 12'
    },
    {
      icon: '🏋️',
      title: 'Workouts',
      value: '73',
      trend: '27.3%',
      isPositive: true,
      highlighted: false,
      sparkline: 'M2 18 L16 22 L30 10 L44 15 L58 6 L78 9'
    },
    {
      icon: '⚡',
      title: 'Sets Completed',
      value: '1,838',
      trend: '13.2%',
      isPositive: true,
      highlighted: false,
      sparkline: 'M2 20 L18 16 L32 19 L48 11 L62 14 L78 5'
    },
    {
      icon: '⏱️',
      title: 'Avg Duration',
      value: '54.7 min',
      trend: '26.4%',
      isPositive: false,
      highlighted: false,
      sparkline: 'M2 6 L18 12 L34 9 L50 20 L64 17 L78 24'
    },
    {
      icon: '💪',
      title: 'Total Volume',
      value: '31,753 kg',
      trend: '14%',
      isPositive: true,
      highlighted: false,
      sparkline: 'M2 19 L16 15 L32 21 L48 12 L64 14 L78 7'
    },
    {
      icon: '❤️',
      title: 'Recovery Score',
      value: '86.3%',
      trend: '+2.3 pts',
      isPositive: true,
      highlighted: false,
      sparkline: 'M2 16 L16 14 L30 18 L46 10 L62 12 L78 6'
    }
  ];

  ngOnInit(): void {
    this.loadFreshProfile();

    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.isDarkMode.set(true);
      document.documentElement.setAttribute('data-theme', 'dark');
    }
  }

  loadFreshProfile(): void {
    this.userService.getProfile().subscribe({
      next: (response) => {
        this.currentUser.set(response.data);
        this.isLoading.set(false);
      },
      error: () => {
        this.logout();
      }
    });
  }

  onTopTabChange(tab: string): void {
    this.selectedTopTab.set(tab);
  }

  onRangeChange(range: string): void {
    this.selectedRange.set(range);
  }

  onGranularityChange(gran: string): void {
    this.selectedGranularity.set(gran);
  }

  private buildMonotoneCubicPath(points: { x: number; y: number }[], closeArea: boolean): string {
    if (!points.length) return '';
    let d = `M ${points[0].x} ${points[0].y}`;

    for (let i = 0; i < points.length - 1; i++) {
      const curr = points[i];
      const next = points[i + 1];
      const ctrlX1 = Math.round(curr.x + (next.x - curr.x) * 0.45);
      const ctrlX2 = Math.round(curr.x + (next.x - curr.x) * 0.55);
      d += ` C ${ctrlX1} ${curr.y}, ${ctrlX2} ${next.y}, ${next.x} ${next.y}`;
    }

    if (closeArea) {
      const last = points[points.length - 1];
      const first = points[0];
      d += ` L ${last.x} 220 L ${first.x} 220 Z`;
    }
    return d;
  }

  setTheme(dark: boolean): void {
    if (this.isDarkMode() === dark) return;

    const applyTheme = () => {
      this.isDarkMode.set(dark);
      if (dark) {
        document.documentElement.setAttribute('data-theme', 'dark');
        localStorage.setItem('theme', 'dark');
      } else {
        document.documentElement.removeAttribute('data-theme');
        localStorage.setItem('theme', 'light');
      }
    };

    // Trigger the GPU hardware crossfade
    const doc = document as Document & { startViewTransition?: (cb: () => void) => void };
    if (doc.startViewTransition) {
      doc.startViewTransition(() => applyTheme());
    } else {
      applyTheme();
    }
  }

  toggleCompare(): void {
    this.compareMode.update(v => !v);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
import { Component, input } from '@angular/core';

@Component({
  selector: 'app-neu-stat-card',
  standalone: true,
  template: `
    <div class="kpi-card neu-raised" [class.highlighted]="highlighted()">
      <div class="kpi-header">
        <span class="kpi-icon neu-inset">{{ icon() }}</span>
        <span class="kpi-title">{{ title() }}</span>
      </div>

      <div class="kpi-value">{{ value() }}</div>

      <div class="kpi-footer">
        <span class="kpi-trend" [class.positive]="isPositive()" [class.negative]="!isPositive()">
          {{ isPositive() ? '↗' : '↘' }} {{ trend() }}
        </span>

        <svg class="sparkline" viewBox="0 0 80 28" fill="none">
          <path 
            [attr.d]="sparklinePath()" 
            [attr.stroke]="isPositive() ? 'var(--accent-color)' : 'var(--error-color)'" 
            stroke-width="2.5" 
            stroke-linecap="round"
            stroke-linejoin="round" 
          />
        </svg>
      </div>
    </div>
  `,
  styles: [`
    .kpi-card {
      padding: 1.1rem 1.25rem;
      border-radius: 18px;
      display: flex;
      flex-direction: column;
      gap: 0.65rem;
      border: 1px solid var(--edge-border);
      transition: transform 0.2s ease, border-color 0.2s ease;

      &:hover {
        transform: translateY(-2px);
      }
      &.highlighted {
        /* Removed the blue border completely for a cleaner look */
      }
    }
    .kpi-header {
      display: flex;
      align-items: center;
      gap: 0.6rem;
    }
    .kpi-icon {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.85rem;
    }
    .kpi-title {
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--text-muted);
    }
    .kpi-value {
      font-size: 1.45rem;
      font-weight: 800;
      color: var(--text-main);
      letter-spacing: -0.5px;
    }
    .kpi-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 0.2rem;
    }
    .kpi-trend {
      font-size: 0.75rem;
      font-weight: 700;
      &.positive { color: #2ecc71; }
      &.negative { color: var(--error-color); }
    }
    .sparkline {
      width: 68px;
      height: 24px;
      overflow: visible;
    }
  `]
})
export class NeuStatCardComponent {
  icon = input.required<string>();
  title = input.required<string>();
  value = input.required<string>();
  trend = input.required<string>();
  isPositive = input<boolean>(true);
  highlighted = input<boolean>(false);
  sparklinePath = input<string>('M2 20 L15 12 L28 18 L42 6 L56 14 L70 4 L78 10');
}
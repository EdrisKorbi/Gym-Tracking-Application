import { Component, input, output } from '@angular/core';
import { NeuSpinnerComponent } from '../neu-spinner/neu-spinner.component';

export type NeuButtonVariant = 'primary' | 'secondary' | 'pill' | 'icon' | 'danger';

@Component({
  selector: 'app-neu-button',
  standalone: true,
  imports: [NeuSpinnerComponent],
  template: `
    <button
      [type]="type()"
      class="neu-button"
      [class.variant-primary]="variant() === 'primary'"
      [class.variant-secondary]="variant() === 'secondary'"
      [class.variant-pill]="variant() === 'pill'"
      [class.variant-icon]="variant() === 'icon'"
      [class.variant-danger]="variant() === 'danger'"
      [class.is-active]="active()"
      [class.full-width]="fullWidth()"
      [disabled]="disabled() || loading()"
      (click)="clicked.emit($event)"
    >
      @if (loading()) {
        <app-neu-spinner [size]="16" />
        @if (loadingText()) {
          <span>{{ loadingText() }}</span>
        }
      } @else {
        <ng-content />
      }
      @if (badge()) {
        <span class="badge">{{ badge() }}</span>
      }
    </button>
  `,
  styles: [`
    :host {
      display: inline-flex;
      flex-shrink: 0;
      &.w-full { width: 100%; }
    }
    .neu-button {
      position: relative;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.45rem;
      height: 38px;
      padding: 0 1.15rem;
      border-radius: 999px;
      border: 1px solid var(--edge-border);
      outline: none;
      cursor: pointer;
      font-family: inherit;
      font-weight: 600;
      font-size: 0.82rem;
      white-space: nowrap;
      flex-shrink: 0;
      background-color: var(--surface-color);
      color: var(--text-main);
      box-shadow: 3px 3px 7px var(--shadow-dark), -2px -2px 6px var(--shadow-light);
      transition: background-color 0.18s ease, color 0.18s ease, border-color 0.18s ease, transform 0.15s cubic-bezier(0.22, 1, 0.36, 1);

      /* Clean Neumorphic hover: subtle surface lift, NO ugly blue border */
      &:hover:not(:disabled):not(.is-active) {
        background-color: var(--surface-hover);
        color: var(--text-main);
        border-color: var(--edge-border);
        transform: translateY(-1px);
        box-shadow: 4px 4px 9px var(--shadow-dark), -3px -3px 7px var(--shadow-light);
      }

      &:active:not(:disabled),
      &.is-active {
        background-color: var(--surface-inset);
        color: var(--text-main);
        border-color: var(--inset-border);
        box-shadow: 
          inset 4px 4px 7px var(--shadow-dark), 
          inset -3px -3px 6px var(--shadow-light);
        transform: translateY(0);
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .neu-button.variant-primary {
      background-color: var(--accent-color);
      color: #ffffff;
      border-color: rgba(255, 255, 255, 0.18);
      box-shadow: 3px 3px 7px var(--shadow-dark), -2px -2px 6px var(--shadow-light);

      &:hover:not(:disabled):not(.is-active) {
        background-color: var(--accent-hover);
        color: #ffffff;
        border-color: rgba(255, 255, 255, 0.3);
      }

      &:active:not(:disabled) {
        background-color: var(--accent-hover);
        color: #ffffff;
        border-color: transparent;
        box-shadow: 
          inset 3px 3px 6px rgba(0, 0, 0, 0.35), 
          inset -2px -2px 5px rgba(255, 255, 255, 0.12);
      }
    }

    .neu-button.variant-danger {
      color: var(--error-color);

      &:hover:not(:disabled):not(.is-active) {
        background-color: var(--error-color);
        color: #ffffff;
        border-color: var(--error-color);
      }

      &:active:not(:disabled) {
        background-color: var(--error-color);
        color: #ffffff;
        box-shadow: inset 3px 3px 6px rgba(0, 0, 0, 0.35);
      }
    }

    .variant-pill { border-radius: 999px; }
    .variant-icon { width: 40px; height: 40px; padding: 0; border-radius: 50%; }
    .full-width { width: 100%; height: 44px; border-radius: 12px; }

    .badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background: var(--error-color);
      color: #fff;
      font-size: 0.65rem;
      font-weight: 700;
      min-width: 18px;
      height: 18px;
      border-radius: 999px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 4px;
      z-index: 2;
    }
  `]
})
export class NeuButtonComponent {
  variant = input<NeuButtonVariant>('secondary');
  type = input<'button' | 'submit'>('button');
  loading = input<boolean>(false);
  loadingText = input<string>('');
  disabled = input<boolean>(false);
  fullWidth = input<boolean>(false);
  active = input<boolean>(false);
  badge = input<string | number | null>(null);
  clicked = output<MouseEvent>();
}
import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-neu-segmented',
  standalone: true,
  template: `
    <div 
      class="segmented-track neu-inset"
      [style.--tab-count]="options().length"
      [style.--active-index]="activeIndex()"
    >
      <div class="sliding-pill"></div>

      @for (option of options(); track option) {
        <button
          type="button"
          class="segment-btn"
          [class.active]="selected() === option"
          (click)="selectedChange.emit(option)"
        >
          {{ option }}
        </button>
      }
    </div>
  `,
  styles: [`
    :host {
      display: inline-flex;
      flex-shrink: 0;
    }
    .segmented-track {
      position: relative;
      display: grid;
      grid-auto-flow: column;
      grid-auto-columns: 1fr;
      align-items: center;
      height: 38px;
      padding: 4px;
      border-radius: 999px;
      box-sizing: border-box;
      user-select: none;
      overflow: hidden;
    }
    .sliding-pill {
      position: absolute;
      top: 4px;
      bottom: 4px;
      left: 4px;
      width: calc((100% - 8px) / var(--tab-count, 3));
      border-radius: 999px;
      background-color: var(--accent-color);
      /* Pure hardware drop shadow. NO blue glow. */
      box-shadow: 2px 2px 5px var(--shadow-dark);
      transform: translateX(calc(var(--active-index, 0) * 100%));
      transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
      pointer-events: none;
      z-index: 1;
    }
    .segment-btn {
      position: relative;
      z-index: 2;
      height: 100%;
      border: none;
      outline: none;
      background: transparent;
      color: var(--text-muted);
      font-family: inherit;
      font-weight: 600;
      font-size: 0.82rem;
      padding: 0 1.15rem;
      border-radius: 999px;
      cursor: pointer;
      white-space: nowrap;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      transition: color 0.18s ease;

      &:hover:not(.active) {
        color: var(--text-main);
      }
      &.active {
        color: #ffffff;
      }
    }
  `]
})
export class NeuSegmentedComponent {
  options = input.required<string[]>();
  selected = input.required<string>();
  selectedChange = output<string>();

  activeIndex = computed(() => {
    const idx = this.options().indexOf(this.selected());
    return idx >= 0 ? idx : 0;
  });
}
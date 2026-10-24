import { Component, input } from '@angular/core';

@Component({
    selector: 'app-neu-spinner',
    standalone: true,
    template: `
    <span 
      class="spinner" 
      [style.width.px]="size()" 
      [style.height.px]="size()"
      [attr.aria-label]="label()">
    </span>
  `,
    styles: [`
    :host {
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .spinner {
      border: 2.5px solid rgba(255, 255, 255, 0.25);
      border-top-color: currentColor;
      border-radius: 50%;
      animation: spin 0.75s linear infinite;
      box-sizing: border-box;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class NeuSpinnerComponent {
    size = input<number>(18);
    label = input<string>('Loading...');
}
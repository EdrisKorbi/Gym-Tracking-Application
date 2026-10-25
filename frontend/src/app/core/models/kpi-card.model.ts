// src/app/core/models/kpi-card.model.ts

export interface KpiCard {
    icon: string;
    title: string;
    value: string;
    trend: string;
    isPositive: boolean;
    highlighted: boolean;
    sparkline: string;
}
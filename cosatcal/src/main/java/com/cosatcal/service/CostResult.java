package com.cosatcal.service;

public record CostResult(
    double netWeightKg,
    double netCost,
    double grossWeightKg,
    double grossCost,
    double grossScrapWeightKg,
    double grossScrapCost,
    double grossMarkedUpCost,
    double finalMaterialCost,
    double cuttingTimeMin,
    double machineCost,
    double gasHourlyCost,
    double gasCost,
    double gasMarkedUpCost,
    double total) {
}

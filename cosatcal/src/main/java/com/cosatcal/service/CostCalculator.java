package com.cosatcal.service;

import com.cosatcal.model.Parameters;
import com.cosatcal.model.Part;
import com.cosatcal.model.Thickness;

public final class CostCalculator {

  private CostCalculator() {
  }

  public static CostResult calculate(
      Part part, Parameters params, Thickness thickness, double pricePerKg) {

    if (params.gasPackM3() <= 0) {
      throw new IllegalArgumentException("gasPackM3 must be greater than 0");
    }

    double kgM2 = thickness.weightKgM2();
    double scrapFactor = 1 + params.scrapPct() / 100;
    double materialMarkup = 1 + params.materialMarkupPct() / 100;
    double gasMarkup = 1 + params.gasMarkupPct() / 100;

    // Net weight comes from the area; NaN if the area was left empty
    double netWeight = Double.isNaN(part.areaMm2()) ? Double.NaN : part.areaMm2() * 1e-6 * kgM2;
    double netCost = netWeight * pricePerKg;

    // Gross weight comes from length x width
    double grossAreaM2 = (part.lengthMm() / 1000) * (part.widthMm() / 1000);
    double grossWeight = grossAreaM2 * kgM2;
    double grossCost = grossWeight * pricePerKg;

    double grossScrapWeight = grossWeight * scrapFactor;
    double grossScrapCost = grossScrapWeight * pricePerKg;

    double grossMarkedUpCost = grossCost * materialMarkup;
    double finalMaterialCost = grossScrapCost * materialMarkup;

    // Machine
    double machineCost = params.machineCostPerHourEur().doubleValue() * part.cuttingTimeMin() / 60;

    // Gas: hourly cost = pack cost x consumption / pack volume
    double gasHourlyCost = params.gasPackCostEur().doubleValue() * thickness.gasConsumptionM3h() / params.gasPackM3();
    double gasCost = gasHourlyCost * part.cuttingTimeMin() / 60;
    double gasMarkedUpCost = gasCost * gasMarkup;

    double total = finalMaterialCost + machineCost + gasMarkedUpCost;

    return new CostResult(
        netWeight, netCost,
        grossWeight, grossCost,
        grossScrapWeight, grossScrapCost,
        grossMarkedUpCost, finalMaterialCost,
        part.cuttingTimeMin(), machineCost,
        gasHourlyCost, gasCost, gasMarkedUpCost,
        total);
  }
}

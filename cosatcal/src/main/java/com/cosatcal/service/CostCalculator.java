package com.cosatcal.service;

import com.cosatcal.model.Parameters;
import com.cosatcal.model.Part;
import com.cosatcal.model.Thickness;

public final class CostCalculator {

  private CostCalculator() {
  }

  /** Full calculation: material + machine + gas. */
  public static CostResult calculate(
      Part part, Parameters params, Thickness thickness, double pricePerKg) {
    return calculate(part, params, thickness, pricePerKg, true, true);
  }

  /**
   * Parts that are not requested are returned as NaN.
   * withMaterial needs length, width and price; withMachine needs the cutting
   * time.
   */
  public static CostResult calculate(
      Part part, Parameters params, Thickness thickness, double pricePerKg,
      boolean withMaterial, boolean withMachine) {

    if (withMachine && params.gasPackM3() <= 0) {
      throw new IllegalArgumentException("gasPackM3 must be greater than 0");
    }

    double netWeight = Double.NaN;
    double netCost = Double.NaN;
    double grossWeight = Double.NaN;
    double grossCost = Double.NaN;
    double grossScrapWeight = Double.NaN;
    double grossScrapCost = Double.NaN;
    double grossMarkedUpCost = Double.NaN;
    double finalMaterialCost = Double.NaN;

    if (withMaterial) {
      double kgM2 = thickness.weightKgM2();
      double scrapFactor = 1 + params.scrapPct() / 100;
      double materialMarkup = 1 + params.materialMarkupPct() / 100;

      // Net weight comes from the area; NaN if the area was left empty
      netWeight = Double.isNaN(part.areaMm2()) ? Double.NaN : part.areaMm2() * 1e-6 * kgM2;
      netCost = netWeight * pricePerKg;

      // Gross weight comes from length x width
      double grossAreaM2 = (part.lengthMm() / 1000) * (part.widthMm() / 1000);
      grossWeight = grossAreaM2 * kgM2;
      grossCost = grossWeight * pricePerKg;

      grossScrapWeight = grossWeight * scrapFactor;
      grossScrapCost = grossScrapWeight * pricePerKg;

      grossMarkedUpCost = grossCost * materialMarkup;
      finalMaterialCost = grossScrapCost * materialMarkup;
    }

    double cuttingTime = Double.NaN;
    double machineCost = Double.NaN;
    double gasHourlyCost = Double.NaN;
    double gasCost = Double.NaN;
    double gasMarkedUpCost = Double.NaN;

    if (withMachine) {
      double gasMarkup = 1 + params.gasMarkupPct() / 100;

      cuttingTime = part.cuttingTimeMin();
      machineCost = params.machineCostPerHourEur().doubleValue() * cuttingTime / 60;

      // Gas: hourly cost = pack cost x consumption / pack volume
      gasHourlyCost = params.gasPackCostEur().doubleValue()
          * thickness.gasConsumptionM3h() / params.gasPackM3();
      gasCost = gasHourlyCost * cuttingTime / 60;
      gasMarkedUpCost = gasCost * gasMarkup;
    }

    double total = (withMaterial ? finalMaterialCost : 0)
        + (withMachine ? machineCost + gasMarkedUpCost : 0);

    return new CostResult(
        netWeight, netCost,
        grossWeight, grossCost,
        grossScrapWeight, grossScrapCost,
        grossMarkedUpCost, finalMaterialCost,
        cuttingTime, machineCost,
        gasHourlyCost, gasCost, gasMarkedUpCost,
        total);
  }
}

package com.cosatcal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosatcal.model.Parameters;
import com.cosatcal.model.Part;
import com.cosatcal.model.Thickness;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CostCalculatorTest {

  private static final double DELTA = 1e-9;

  // Inox, 2 mm, 147 x 20 mm, area 2430 mm², 1.5 min, 3 EUR/kg
  private static final Part PART = new Part(147, 20, 2430, 1.5);
  private static final Thickness THICKNESS = new Thickness(2, "14301", 15.7, 24);
  private static final double PRICE = 3;

  private static Parameters params(double scrap, double materialMarkup, double gasMarkup,
      double packM3) {
    return new Parameters(
        scrap, materialMarkup, gasMarkup,
        BigDecimal.valueOf(200), BigDecimal.valueOf(170), packM3);
  }

  private static Parameters defaultParams() {
    return params(20, 30, 30, 168);
  }

  @Test
  void calculatesAllMaterialValues() {
    CostResult r = CostCalculator.calculate(PART, defaultParams(), THICKNESS, PRICE);

    assertEquals(0.038151, r.netWeightKg(), DELTA);
    assertEquals(0.114453, r.netCost(), DELTA);
    assertEquals(0.046158, r.grossWeightKg(), DELTA);
    assertEquals(0.138474, r.grossCost(), DELTA);
    assertEquals(0.0553896, r.grossScrapWeightKg(), DELTA);
    assertEquals(0.1661688, r.grossScrapCost(), DELTA);
    assertEquals(0.1800162, r.grossMarkedUpCost(), DELTA);
    assertEquals(0.21601944, r.finalMaterialCost(), DELTA);
  }

  @Test
  void calculatesMachineAndGas() {
    CostResult r = CostCalculator.calculate(PART, defaultParams(), THICKNESS, PRICE);

    assertEquals(1.5, r.cuttingTimeMin(), DELTA);
    assertEquals(5.0, r.machineCost(), DELTA); // 200 * 1.5 / 60
    assertEquals(170.0 * 24 / 168, r.gasHourlyCost(), DELTA); // 24.2857 EUR/h
    assertEquals(170.0 * 24 / 168 * 1.5 / 60, r.gasCost(), DELTA);
    assertEquals(170.0 * 24 / 168 * 1.5 / 60 * 1.3, r.gasMarkedUpCost(), DELTA);
  }

  @Test
  void totalIsMaterialPlusMachinePlusGas() {
    CostResult r = CostCalculator.calculate(PART, defaultParams(), THICKNESS, PRICE);

    assertEquals(r.finalMaterialCost() + r.machineCost() + r.gasMarkedUpCost(), r.total(), DELTA);
    assertEquals(6.0053, r.total(), 1e-4); // shows as "6,01 €"
  }

  @Test
  void zeroScrapAndMarkupsLeaveMaterialCostUnchanged() {
    CostResult r = CostCalculator.calculate(PART, params(0, 0, 0, 168), THICKNESS, PRICE);

    assertEquals(r.grossWeightKg(), r.grossScrapWeightKg(), DELTA);
    assertEquals(r.grossCost(), r.finalMaterialCost(), DELTA);
    assertEquals(r.gasCost(), r.gasMarkedUpCost(), DELTA);
  }

  @Test
  void emptyAreaGivesNaNNetValuesButKeepsTheRest() {
    Part noArea = new Part(147, 20, Double.NaN, 1.5);

    CostResult r = CostCalculator.calculate(noArea, defaultParams(), THICKNESS, PRICE);

    assertTrue(Double.isNaN(r.netWeightKg()));
    assertTrue(Double.isNaN(r.netCost()));
    assertEquals(0.21601944, r.finalMaterialCost(), DELTA);
    assertEquals(6.0053, r.total(), 1e-4);
  }

  @Test
  void zeroCuttingTimeGivesNoMachineOrGasCost() {
    Part noTime = new Part(147, 20, 2430, 0);

    CostResult r = CostCalculator.calculate(noTime, defaultParams(), THICKNESS, PRICE);

    assertEquals(0, r.machineCost(), DELTA);
    assertEquals(0, r.gasCost(), DELTA);
    assertEquals(r.finalMaterialCost(), r.total(), DELTA);
  }

  @Test
  void rejectsGasPackWithoutVolume() {
    assertThrows(IllegalArgumentException.class,
        () -> CostCalculator.calculate(PART, params(20, 30, 30, 0), THICKNESS, PRICE));
  }
}

package com.cosatcal.controller;

import com.cosatcal.model.Material;
import com.cosatcal.model.Parameters;
import com.cosatcal.model.Part;
import com.cosatcal.model.Thickness;
import com.cosatcal.repository.DataLoadException;
import com.cosatcal.repository.ExcelRepository;
import com.cosatcal.service.CostCalculator;
import com.cosatcal.service.CostResult;
import com.cosatcal.view.InputPanel;
import com.cosatcal.view.MainView;
import com.cosatcal.view.ResultPanel;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class MainController {

  // Messages shown to the user
  private static final String MSG_NOT_LOADED = "I dati non sono stati caricati.";
  private static final String MSG_SELECT = "Seleziona materiale e spessore.";
  private static final String MSG_NO_PRICE = "Nessun prezzo per questo materiale.";
  private static final String MSG_NO_WEIGHT = "Nessun peso (kg/m²) per questo spessore.";
  private static final String MSG_NO_GAS = "Nessun consumo di gas per questo spessore.";

  private static final String NOT_AVAILABLE = "n/d";

  private final MainView view;
  private final InputPanel input;
  private final ResultPanel result;
  private final ExcelRepository repository;
  private final String sourceName;
  private boolean loaded;

  public MainController(MainView view, ExcelRepository repository, String sourceName) {
    this.view = view;
    this.input = view.inputPanel();
    this.result = view.resultPanel();
    this.repository = repository;
    this.sourceName = sourceName;

    input.materialCombo().valueProperty().addListener(
        (obs, old, now) -> fillThicknesses(now, null));
    input.thicknessCombo().valueProperty().addListener((obs, old, now) -> showThicknessInfo());
    input.calculateButton().setOnAction(e -> calculate());
    view.reloadButton().setOnAction(e -> reload());

    reload();
  }

  // ---------- Loading ----------

  /**
   * Reads the Excel again and refills the panel. On error the old data is kept.
   */
  private void reload() {
    try {
      repository.load();
    } catch (DataLoadException e) {
      showError(e.getMessage());
      return;
    }
    loaded = true;

    String selectedMaterial = input.materialCombo().getValue();
    String selectedThickness = input.thicknessCombo().getValue();

    fillParameters(repository.parameters());

    Map<String, Double> costs = new LinkedHashMap<>();
    for (Material m : repository.materials()) {
      costs.put(m.name(), m.pricePerKgEur() == null ? 0 : m.pricePerKgEur().doubleValue());
    }
    input.setMaterialCosts(costs);

    boolean stillExists = selectedMaterial != null && costs.containsKey(selectedMaterial);
    input.materialCombo().setValue(stillExists ? selectedMaterial : null);
    fillThicknesses(stillExists ? selectedMaterial : null, selectedThickness);

    result.setSource("Dati letti da " + sourceName);
  }

  private void fillParameters(Parameters p) {
    input.extraSfridoField().setText(plain(p.scrapPct()));
    input.extraMaterialField().setText(plain(p.materialMarkupPct()));
    input.extraGasField().setText(plain(p.gasMarkupPct()));
    input.machineCostField().setText(plain(p.machineCostPerHourEur().doubleValue()));
    input.packageCostField().setText(plain(p.gasPackCostEur().doubleValue()));
  }

  private void fillThicknesses(String materialName, String keep) {
    input.thicknessCombo().getItems().clear();
    if (materialName != null) {
      for (Thickness t : repository.thicknesses(materialName)) {
        input.thicknessCombo().getItems().add(plain(t.thicknessMm()));
      }
    }
    if (keep != null && input.thicknessCombo().getItems().contains(keep)) {
      input.thicknessCombo().setValue(keep);
    }
    showThicknessInfo();
  }

  private void showThicknessInfo() {
    Thickness t = selectedThickness();
    if (t == null) {
      input.infoLabel().setText("");
      return;
    }
    input.infoLabel().setText(
        "Codice " + (t.code().isEmpty() ? NOT_AVAILABLE : t.code())
            + " · " + (Double.isNaN(t.weightKgM2()) ? NOT_AVAILABLE : plain(t.weightKgM2()))
            + " kg/m² · "
            + (Double.isNaN(t.gasConsumptionM3h()) ? NOT_AVAILABLE : plain(t.gasConsumptionM3h()))
            + " m³/h");
  }

  private Thickness selectedThickness() {
    String material = input.materialCombo().getValue();
    String selected = input.thicknessCombo().getValue();
    if (!loaded || material == null || selected == null) {
      return null;
    }
    for (Thickness t : repository.thicknesses(material)) {
      if (plain(t.thicknessMm()).equals(selected)) {
        return t;
      }
    }
    return null;
  }

  // ---------- Calculation ----------

  private void calculate() {
    try {
      if (!loaded) {
        throw new IllegalArgumentException(MSG_NOT_LOADED);
      }

      String materialName = input.materialCombo().getValue();
      Thickness thickness = selectedThickness();
      if (materialName == null || thickness == null) {
        throw new IllegalArgumentException(MSG_SELECT);
      }

      // Price typed by the user, or the Excel one if the field was left untouched
      double price = input.materialCosts().getOrDefault(materialName, 0.0);
      if (price <= 0) {
        throw new IllegalArgumentException(MSG_NO_PRICE);
      }
      if (Double.isNaN(thickness.weightKgM2())) {
        throw new IllegalArgumentException(MSG_NO_WEIGHT);
      }
      if (Double.isNaN(thickness.gasConsumptionM3h())) {
        throw new IllegalArgumentException(MSG_NO_GAS);
      }

      Part part = new Part(
          required(input.lengthField(), "Lunghezza"),
          required(input.widthField(), "Larghezza"),
          optional(input.netAreaField(), "Area netta"),
          required(input.cuttingTimeField(), "Tempo di taglio"));

      Parameters params = new Parameters(
          required(input.extraSfridoField(), "Extra sfrido"),
          required(input.extraMaterialField(), "Extra materiale"),
          required(input.extraGasField(), "Extra gas"),
          BigDecimal.valueOf(required(input.machineCostField(), "Costo macchina")),
          BigDecimal.valueOf(required(input.packageCostField(), "Costo per pacco")),
          repository.parameters().gasPackM3()); // m³ of the pack come from the Excel only

      CostResult r = CostCalculator.calculate(part, params, thickness, price);
      show(r, params, materialName, price);

    } catch (IllegalArgumentException e) {
      showError(e.getMessage());
    }
  }

  // ---------- Showing results ----------

  private void show(CostResult r, Parameters p, String materialName, double price) {
    result.setTotal(money(r.total()));

    result.setMaterial(
        "Peso " + weight(r.grossScrapWeightKg()) + " (lungh. × largh. + sfrido "
            + plain(p.scrapPct()) + "%) · ricarico " + plain(p.materialMarkupPct()) + "%",
        money(r.finalMaterialCost()));
    result.setMachine(
        plain(r.cuttingTimeMin()) + " min × " + plain(p.machineCostPerHourEur().doubleValue())
            + " €/h",
        money(r.machineCost()));
    result.setGas(
        plain(r.cuttingTimeMin()) + " min × " + money(r.gasHourlyCost()) + "/h · ricarico "
            + plain(p.gasMarkupPct()) + "%",
        money(r.gasMarkedUpCost()));

    result.setDetail("netWeight", weight(r.netWeightKg()));
    result.setDetail("netCost", money(r.netCost()));
    result.setDetail("grossWeight", weight(r.grossWeightKg()));
    result.setDetail("grossCost", money(r.grossCost()));
    result.setDetail("grossScrapWeight", weight(r.grossScrapWeightKg()));
    result.setDetail("grossScrapCost", money(r.grossScrapCost()));
    result.setDetail("grossMarkedUpCost", money(r.grossMarkedUpCost()));
    result.setDetail("finalMaterialCost", money(r.finalMaterialCost()));
    result.setDetail("cuttingTime", plain(r.cuttingTimeMin()) + " min");
    result.setDetail("machineCost", money(r.machineCost()));
    result.setDetail("gas", money(r.gasCost()));
    result.setDetail("gasMarkedUp", money(r.gasMarkedUpCost()));

    result.setSource(isPriceModified(materialName, price)
        ? "Calcolo con costo modificato manualmente"
        : "Dati letti da " + sourceName);
  }

  private boolean isPriceModified(String materialName, double price) {
    for (Material m : repository.materials()) {
      if (m.name().equals(materialName) && m.pricePerKgEur() != null) {
        return m.pricePerKgEur().doubleValue() != price;
      }
    }
    return false;
  }

  // ---------- Helpers ----------

  /** Reads a mandatory number. Accepts "1,5" and "1.5". */
  private static double required(TextField field, String name) {
    String text = field.getText() == null ? "" : field.getText().trim();
    if (text.isEmpty()) {
      throw new IllegalArgumentException("Il campo '" + name + "' è vuoto.");
    }
    try {
      return Double.parseDouble(text.replace(',', '.'));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Il campo '" + name + "' non è un numero: " + text);
    }
  }

  /** Reads an optional number. Empty -> NaN. */
  private static double optional(TextField field, String name) {
    String text = field.getText() == null ? "" : field.getText().trim();
    return text.isEmpty() ? Double.NaN : required(field, name);
  }

  /** 20.0 -> "20", 1.5 -> "1,5" */
  private static String plain(double value) {
    return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().replace('.', ',');
  }

  private static String money(double value) {
    return String.format(Locale.ITALY, "%.2f €", value);
  }

  private static String weight(double value) {
    return Double.isNaN(value) ? NOT_AVAILABLE : String.format(Locale.ITALY, "%.3f kg", value);
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR, message);
    if (view.getScene() != null) {
      alert.initOwner(view.getScene().getWindow());
    }
    alert.show();
  }
}

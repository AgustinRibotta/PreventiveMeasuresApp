package com.cosatcal.view;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;

public class InputPanel extends VBox {

  // Materiale
  private final ComboBox<String> materialCombo = new ComboBox<>();
  private final ComboBox<String> thicknessCombo = new ComboBox<>();
  private final Label infoLabel = new Label();

  // Pezzo
  private final TextField lengthField = new TextField();
  private final TextField widthField = new TextField();
  private final TextField netAreaField = new TextField();
  private final TextField cuttingTimeField = new TextField();

  // Parametri generali
  private final TextField extraSfridoField = new TextField();
  private final TextField machineCostField = new TextField();
  private final TextField extraMaterialField = new TextField();
  private final TextField extraGasField = new TextField();
  private final TextField packageCostField = new TextField();

  // Costi materiali (vengono dall'Excel, l'utente può modificarli)
  private final Map<String, Double> excelCosts = new LinkedHashMap<>();
  private final Map<String, TextField> costFields = new LinkedHashMap<>();
  private final GridPane costsGrid = grid();

  private final Button calculateButton = new Button("Calcola");

  public InputPanel() {
    getStyleClass().add("card");
    setSpacing(8);
    setPrefWidth(290);
    setMinWidth(290);
    setMaxWidth(290);

    infoLabel.getStyleClass().add("info-box");
    infoLabel.setMaxWidth(Double.MAX_VALUE);

    calculateButton.getStyleClass().add("primary-button");
    calculateButton.setMaxWidth(Double.MAX_VALUE);

    VBox materialContent = new VBox(8,
        sectionTitle("MATERIALE"),
        twoColumns(
            labeled("Materiale", materialCombo),
            labeled("Spessore (mm)", thicknessCombo)),
        infoLabel,
        sectionTitle("PEZZO"),
        twoColumns(
            labeled("Lunghezza (mm)", lengthField),
            labeled("Larghezza (mm)", widthField),
            labeled("Area netta (mm²)", netAreaField),
            labeled("Tempo taglio (min)", cuttingTimeField)));
    materialContent.setPadding(new Insets(10));

    VBox parametersContent = new VBox(8,
        sectionTitle("PARAMETRI GENERALI"),
        twoColumns(
            labeled("Extra sfrido (%)", extraSfridoField),
            labeled("Costo macchina (€/h)", machineCostField),
            labeled("Extra materiale (%)", extraMaterialField),
            labeled("Extra gas (%)", extraGasField),
            labeled("Costo per pacco (€)", packageCostField)),
        sectionTitle("COSTI MATERIALI"),
        costsGrid);
    parametersContent.setPadding(new Insets(10));

    Tab materialTab = new Tab("MATERIALE", scrollable(materialContent));
    Tab parametersTab = new Tab("PARAMETRI", scrollable(parametersContent));

    TabPane tabPane = new TabPane(materialTab, parametersTab);
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    getChildren().addAll(tabPane, calculateButton);
  }

  // ---------- Costi materiali ----------

  /** Carga los costos del Excel: nombre del material -> costo. */
  public void setMaterialCosts(Map<String, Double> costs) {
    excelCosts.clear();
    excelCosts.putAll(costs);
    costFields.clear();
    costsGrid.getChildren().clear();

    int i = 0;
    for (Map.Entry<String, Double> e : excelCosts.entrySet()) {
      TextField field = new TextField(String.valueOf(e.getValue()).replace('.', ','));
      costFields.put(e.getKey(), field);
      costsGrid.add(labeled(e.getKey().toLowerCase(), field), i % 2, i / 2);
      i++;
    }

    String selected = materialCombo.getValue();
    materialCombo.getItems().setAll(excelCosts.keySet());
    if (selected != null && excelCosts.containsKey(selected)) {
      materialCombo.setValue(selected);
    }
  }

  /**
   * Costos para calcular: lo que escribió la persona, o el del Excel si está
   * vacío/inválido.
   */
  public Map<String, Double> materialCosts() {
    Map<String, Double> result = new LinkedHashMap<>();
    excelCosts.forEach((name, excelValue) -> {
      Double typed = parse(costFields.get(name).getText());
      result.put(name, typed != null ? typed : excelValue);
    });
    return result;
  }

  private static Double parse(String text) {
    try {
      return Double.parseDouble(text.trim().replace(',', '.'));
    } catch (Exception e) {
      return null;
    }
  }

  // ---------- Helpers ----------

  private Label sectionTitle(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("section-title");
    return label;
  }

  private VBox labeled(String text, Control control) {
    Label label = new Label(text);
    label.getStyleClass().add("field-label");
    control.setMaxWidth(Double.MAX_VALUE);
    return new VBox(3, label, control);
  }

  private static GridPane grid() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);

    ColumnConstraints left = new ColumnConstraints();
    left.setPercentWidth(50);
    ColumnConstraints right = new ColumnConstraints();
    right.setPercentWidth(50);
    grid.getColumnConstraints().addAll(left, right);
    return grid;
  }

  private GridPane twoColumns(Node... nodes) {
    GridPane grid = grid();
    for (int i = 0; i < nodes.length; i++) {
      grid.add(nodes[i], i % 2, i / 2);
    }
    return grid;
  }

  public void showSampleData() {
    Map<String, Double> sample = new LinkedHashMap<>();
    sample.put("Ferro", 0.9);
    sample.put("Inox", 3.2);
    sample.put("Zincato", 1.1);
    sample.put("Inox P", 3.5);
    sample.put("Alluminio", 4.0);
    setMaterialCosts(sample);

    materialCombo.setValue("Inox");
    thicknessCombo.getItems().setAll("0.8", "1", "1.2", "1.5", "2");
    thicknessCombo.setValue("2");
    infoLabel.setText("Codice 14301 · 15,7 kg/m² · 24 m³/h");

    lengthField.setText("147");
    widthField.setText("20");
    netAreaField.setText("2430");
    cuttingTimeField.setText("1,5");
  }

  /** Wraps a tab's content so it scrolls vertically when it does not fit. */
  private ScrollPane scrollable(Node content) {
    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.getStyleClass().add("transparent-scroll");
    return scroll;
  }

  // ---------- Accesores ----------

  public ComboBox<String> materialCombo() {
    return materialCombo;
  }

  public ComboBox<String> thicknessCombo() {
    return thicknessCombo;
  }

  public Label infoLabel() {
    return infoLabel;
  }

  public TextField lengthField() {
    return lengthField;
  }

  public TextField widthField() {
    return widthField;
  }

  public TextField netAreaField() {
    return netAreaField;
  }

  public TextField cuttingTimeField() {
    return cuttingTimeField;
  }

  public Button calculateButton() {
    return calculateButton;
  }

  public TextField extraSfridoField() {
    return extraSfridoField;
  }

  public TextField machineCostField() {
    return machineCostField;
  }

  public TextField extraMaterialField() {
    return extraMaterialField;
  }

  public TextField extraGasField() {
    return extraGasField;
  }

  public TextField packageCostField() {
    return packageCostField;
  }
}

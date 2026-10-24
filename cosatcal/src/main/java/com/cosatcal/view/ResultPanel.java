package com.cosatcal.view;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ResultPanel extends VBox {

  private final Label total = label("0,00 €", "total-bar-value");
  private final Label materialValue = label("", "summary-value");
  private final Label machineValue = label("", "summary-value");
  private final Label gasValue = label("", "summary-value");
  private final Label materialDetail = label("", "muted");
  private final Label machineDetail = label("", "muted");
  private final Label gasDetail = label("", "muted");
  private final Label source = label("", "footer");
  private final Map<String, Label> detailValues = new HashMap<>();

  public ResultPanel() {
    getStyleClass().add("card");
    setSpacing(8);

    VBox material = block("MATERIALE", "block-material", materialGrid());
    VBox machine = block("MACCHINA", "block-machine", machineGrid());
    VBox gas = block("GAS", "block-gas", gasGrid());

    // Macchina and Gas share the width equally
    for (VBox half : new VBox[] { machine, gas }) {
      half.setPrefWidth(100);
      half.setMaxWidth(Double.MAX_VALUE);
      HBox.setHgrow(half, Priority.ALWAYS);
    }
    HBox bottom = new HBox(8, machine, gas);

    getChildren().addAll(totalBar(), summary(), material, bottom, source);
  }

  private static Label label(String text, String... styleClasses) {
    Label label = new Label(text);
    label.getStyleClass().addAll(styleClasses);
    return label;
  }

  // ---- Summary (top) ----

  /** Dark bar with the grand total. */
  private HBox totalBar() {
    Label name = label("TOTALE", "total-bar-text");

    Region grow = new Region();
    HBox.setHgrow(grow, Priority.ALWAYS);

    HBox bar = new HBox(name, grow, total);
    bar.setAlignment(Pos.CENTER_LEFT);
    bar.getStyleClass().add("total-bar");
    return bar;
  }

  private VBox summary() {
    return new VBox(6,
        summaryRow("Materiale", "summary-material", materialDetail, materialValue),
        summaryRow("Macchina", "summary-machine", machineDetail, machineValue),
        summaryRow("Gas", "summary-gas", gasDetail, gasValue));
  }

  /** Title and explanation on the left, value on the right, colored edge. */
  private HBox summaryRow(String title, String colorClass, Label detail, Label value) {
    detail.setWrapText(true);
    detail.setMaxWidth(Double.MAX_VALUE);

    VBox texts = new VBox(1, label(title, "summary-title"), detail);
    HBox.setHgrow(texts, Priority.ALWAYS);

    value.setMinWidth(Region.USE_PREF_SIZE);

    HBox row = new HBox(10, texts, value);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().addAll("summary-row", colorClass);
    return row;
  }

  // ---- Boxes (bottom): data only ----

  private VBox block(String title, String colorClass, Node content) {
    Label name = label(title, "block-title");
    name.setMaxWidth(Double.MAX_VALUE);

    HBox header = new HBox(name);
    header.getStyleClass().addAll("block-header", colorClass);

    VBox body = new VBox(content);
    body.setPadding(new Insets(8));

    VBox box = new VBox(header, body);
    box.getStyleClass().add("detail-block");
    return box;
  }

  private GridPane materialGrid() {
    GridPane grid = new GridPane();
    grid.setVgap(4);
    grid.setHgap(10);

    ColumnConstraints name = new ColumnConstraints();
    name.setHgrow(Priority.ALWAYS);
    ColumnConstraints weight = new ColumnConstraints();
    weight.setHalignment(HPos.RIGHT);
    weight.setMinWidth(Region.USE_PREF_SIZE);
    ColumnConstraints cost = new ColumnConstraints();
    cost.setHalignment(HPos.RIGHT);
    cost.setMinWidth(Region.USE_PREF_SIZE);
    grid.getColumnConstraints().addAll(name, weight, cost);

    grid.add(label("Peso", "muted"), 1, 0);
    grid.add(label("Costo", "muted"), 2, 0);

    weightCostRow(grid, 1, "Netto (da area)", "netWeight", "netCost");
    weightCostRow(grid, 2, "Lordo (lungh. × largh.)", "grossWeight", "grossCost");
    weightCostRow(grid, 3, "Lordo + sfrido", "grossScrapWeight", "grossScrapCost");
    costOnlyRow(grid, 4, "Lordo ricaricato", "grossMarkedUpCost", false);
    costOnlyRow(grid, 5, "Finale (sfrido + ricarico)", "finalMaterialCost", true);
    return grid;
  }

  private void weightCostRow(
      GridPane grid, int row, String title, String weightKey, String costKey) {
    Label weight = label("", "row-plain");
    Label cost = label("", "row-plain");
    detailValues.put(weightKey, weight);
    detailValues.put(costKey, cost);
    grid.add(label(title, "row-plain"), 0, row);
    grid.add(weight, 1, row);
    grid.add(cost, 2, row);
  }

  private void costOnlyRow(GridPane grid, int row, String title, String key, boolean strong) {
    String style = strong ? "row-strong" : "row-plain";
    Label value = label("", style);
    detailValues.put(key, value);
    grid.add(label(title, style), 0, row);
    grid.add(value, 2, row);
  }

  private GridPane machineGrid() {
    GridPane grid = twoColumnGrid();
    line(grid, 0, "cuttingTime", "Tempo di taglio", false);
    line(grid, 1, "machineCost", "Costo macchina", true);
    return grid;
  }

  private GridPane gasGrid() {
    GridPane grid = twoColumnGrid();
    line(grid, 0, "gas", "Gas (senza ricarico)", false);
    line(grid, 1, "gasMarkedUp", "Gas con ricarico", true);
    return grid;
  }

  private GridPane twoColumnGrid() {
    GridPane grid = new GridPane();
    grid.setVgap(4);
    grid.setHgap(10);
    ColumnConstraints left = new ColumnConstraints();
    left.setHgrow(Priority.ALWAYS);
    ColumnConstraints right = new ColumnConstraints();
    right.setHalignment(HPos.RIGHT);
    right.setMinWidth(Region.USE_PREF_SIZE);
    grid.getColumnConstraints().addAll(left, right);
    return grid;
  }

  private void line(GridPane grid, int row, String key, String title, boolean strong) {
    String style = strong ? "row-strong" : "row-plain";
    Label value = label("", style);
    detailValues.put(key, value);
    grid.add(label(title, style), 0, row);
    grid.add(value, 1, row);
  }

  // ---- API for the controller ----

  public void setTotal(String text) {
    total.setText(text);
  }

  public void setMaterial(String detail, String value) {
    materialDetail.setText(detail);
    materialValue.setText(value);
  }

  public void setMachine(String detail, String value) {
    machineDetail.setText(detail);
    machineValue.setText(value);
  }

  public void setGas(String detail, String value) {
    gasDetail.setText(detail);
    gasValue.setText(value);
  }

  public void setSource(String text) {
    source.setText(text);
  }

  /** Keys that are not shown (e.g. "total") are ignored. */
  public void setDetail(String key, String value) {
    Label target = detailValues.get(key);
    if (target != null) {
      target.setText(value);
    }
  }
}

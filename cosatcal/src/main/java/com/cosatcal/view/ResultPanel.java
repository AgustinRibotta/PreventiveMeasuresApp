package com.cosatcal.view;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ResultPanel extends VBox {

    private final Label total = label("0,00 €", "total");
    private final Label materialDetail = label("", "muted");
    private final Label materialValue = label("", "row-value");
    private final Label machineDetail = label("", "muted");
    private final Label machineValue = label("", "row-value");
    private final Label gasDetail = label("", "muted");
    private final Label gasValue = label("", "row-value");
    private final Label totalSum = label("", "row-value", "bold");
    private final Label source = label("", "footer");
    private final Map<String, Label> detailValues = new HashMap<>();

    public ResultPanel() {
        getStyleClass().add("card");
        setSpacing(8);

        TabPane tabs = new TabPane(
                new Tab("Riepilogo", buildSummary()),
                new Tab("Dettaglio completo", buildDetail()));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        getChildren().addAll(tabs, source);
    }

    private static Label label(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    // ---- Summary tab ----

    private VBox buildSummary() {
        HBox head = new HBox(10, total, label("costo totale del pezzo", "muted"));
        head.setAlignment(Pos.BASELINE_LEFT);

        Label totalName = label("Totale", "row-value", "bold");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox totalRow = new HBox(totalName, grow, totalSum);

        return new VBox(14, head, new Separator(),
                summaryRow("Materiale", materialDetail, materialValue),
                summaryRow("Macchina", machineDetail, machineValue),
                summaryRow("Gas", gasDetail, gasValue),
                new Separator(), totalRow);
    }

    private HBox summaryRow(String title, Label subtitle, Label value) {
        VBox texts = new VBox(2, label(title, "row-title"), subtitle);
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox row = new HBox(texts, grow, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ---- Detail tab ----

    private GridPane buildDetail() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        ColumnConstraints left = new ColumnConstraints();
        left.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints();
        right.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().addAll(left, right);

        int r = 0;
        r = section(grid, r, "MATERIALE");
        r = line(grid, r, "netWeight", "Peso netto (da area)", false);
        r = line(grid, r, "netCost", "Costo netto", false);
        r = line(grid, r, "grossWeight", "Peso lordo (lungh. × largh.)", false);
        r = line(grid, r, "grossCost", "Costo lordo", false);
        r = line(grid, r, "grossScrapWeight", "Peso lordo + sfrido", false);
        r = line(grid, r, "grossScrapCost", "Costo lordo + sfrido", false);
        r = line(grid, r, "grossMarkedUpCost", "Costo lordo ricaricato", false);
        r = line(grid, r, "finalMaterialCost", "Costo lordo + sfrido ricaricato", true);
        r = section(grid, r, "MACCHINA");
        r = line(grid, r, "cuttingTime", "Tempo di taglio", false);
        r = line(grid, r, "machineCost", "Costo macchina", true);
        r = section(grid, r, "GAS");
        r = line(grid, r, "gas", "Gas", false);
        r = line(grid, r, "gasMarkedUp", "Gas con ricarico", true);
        r = section(grid, r, "TOTALE");
        line(grid, r, "total", "Materiale + macchina + gas", true);
        return grid;
    }

    private int section(GridPane grid, int row, String title) {
        grid.add(label(title, "section-title"), 0, row, 2, 1);
        return row + 1;
    }

    private int line(GridPane grid, int row, String key, String title, boolean bold) {
        String style = bold ? "bold" : "row-plain";
        Label value = label("", style);
        detailValues.put(key, value);
        grid.add(label(title, style), 0, row);
        grid.add(value, 1, row);
        return row + 1;
    }

    // ---- Temporary sample data ----

    public void showSampleData() {
        total.setText("6,01 €");
        materialDetail.setText("Peso 0,055 kg (lungh. × largh. + sfrido 20%) · ricarico 30%");
        materialValue.setText("0,22 €");
        machineDetail.setText("1,5 min × 200 €/h");
        machineValue.setText("5,00 €");
        gasDetail.setText("1,5 min × 24,29 €/h · ricarico 30%");
        gasValue.setText("0,79 €");
        totalSum.setText("6,01 €");
        source.setText("Dati letti da CosatCal_dati_per_materiale.xlsx");

        detailValues.get("netWeight").setText("0,038 kg");
        detailValues.get("netCost").setText("0,11 €");
        detailValues.get("grossWeight").setText("0,046 kg");
        detailValues.get("grossCost").setText("0,14 €");
        detailValues.get("grossScrapWeight").setText("0,055 kg");
        detailValues.get("grossScrapCost").setText("0,17 €");
        detailValues.get("grossMarkedUpCost").setText("0,18 €");
        detailValues.get("finalMaterialCost").setText("0,22 €");
        detailValues.get("cuttingTime").setText("1,5 min");
        detailValues.get("machineCost").setText("5,00 €");
        detailValues.get("gas").setText("0,61 €");
        detailValues.get("gasMarkedUp").setText("0,79 €");
        detailValues.get("total").setText("6,01 €");
    }
}

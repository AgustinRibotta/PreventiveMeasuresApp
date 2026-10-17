package com.cosatcal.view;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class InputPanel extends VBox {

    private final ComboBox<String> materialCombo = new ComboBox<>();
    private final ComboBox<String> thicknessCombo = new ComboBox<>();
    private final Label infoLabel = new Label();
    private final TextField lengthField = new TextField();
    private final TextField widthField = new TextField();
    private final TextField netAreaField = new TextField();
    private final TextField cuttingTimeField = new TextField();
    private final Button calculateButton = new Button("Calcola");
    private final Button clearButton = new Button("Pulisci");

    public InputPanel() {
        getStyleClass().add("card");
        setSpacing(20);
        setMinWidth(460);
        setPrefWidth(460);
        setMaxWidth(460);

        infoLabel.getStyleClass().add("info-box");
        infoLabel.setMaxWidth(Double.MAX_VALUE);

        calculateButton.getStyleClass().add("primary-button");
        calculateButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(calculateButton, Priority.ALWAYS);
        clearButton.getStyleClass().add("secondary-button");
        HBox buttons = new HBox(12, calculateButton, clearButton);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                sectionTitle("MATERIALE"),
                twoColumns(labeled("Materiale", materialCombo),
                           labeled("Spessore (mm)", thicknessCombo)),
                infoLabel,
                sectionTitle("PEZZO"),
                twoColumns(labeled("Lunghezza (mm)", lengthField),
                           labeled("Larghezza (mm)", widthField),
                           labeled("Area netta (mm²) · opzionale", netAreaField),
                           labeled("Tempo di taglio (min)", cuttingTimeField)),
                spacer,
                buttons);
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private VBox labeled(String text, Control control) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        control.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, label, control);
    }

    private GridPane twoColumns(Node... nodes) {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        grid.getColumnConstraints().addAll(left, right);
        for (int i = 0; i < nodes.length; i++) {
            grid.add(nodes[i], i % 2, i / 2);
        }
        return grid;
    }

    public void showSampleData() {
        materialCombo.getItems().setAll("Ferro", "Inox", "Inox P", "Zincato", "Alluminio");
        materialCombo.setValue("Inox");
        thicknessCombo.getItems().setAll("0.8", "1", "1.2", "1.5", "2");
        thicknessCombo.setValue("2");
        infoLabel.setText("Codice 14301 · 15,7 kg/m² · 24 m³/h");
        lengthField.setText("147");
        widthField.setText("20");
        netAreaField.setText("2430");
        cuttingTimeField.setText("1,5");
    }

    public ComboBox<String> materialCombo() { return materialCombo; }
    public ComboBox<String> thicknessCombo() { return thicknessCombo; }
    public Label infoLabel() { return infoLabel; }
    public TextField lengthField() { return lengthField; }
    public TextField widthField() { return widthField; }
    public TextField netAreaField() { return netAreaField; }
    public TextField cuttingTimeField() { return cuttingTimeField; }
    public Button calculateButton() { return calculateButton; }
    public Button clearButton() { return clearButton; }
}

package com.cosatcal.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MainView extends BorderPane {

    private final InputPanel inputPanel = new InputPanel();
    private final ResultPanel resultPanel = new ResultPanel();
    private final Button reloadButton = new Button("Ricarica dati");

    public MainView() {
        getStyleClass().add("root-pane");
        setTop(buildHeader());

        HBox body = new HBox(20, inputPanel, resultPanel);
        HBox.setHgrow(resultPanel, Priority.ALWAYS);
        body.setPadding(new Insets(16, 24, 24, 24));
        setCenter(body);

        // Temporary: remove when the controller feeds real data
        inputPanel.showSampleData();
        resultPanel.showSampleData();
    }

    private HBox buildHeader() {
        Label title = new Label("CosatCal");
        title.getStyleClass().add("app-title");

        Label date = new Label(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")));
        date.getStyleClass().add("header-date");

        reloadButton.getStyleClass().add("ghost-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(18, title, spacer, date, reloadButton);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    public InputPanel inputPanel() { return inputPanel; }
    public ResultPanel resultPanel() { return resultPanel; }
    public Button reloadButton() { return reloadButton; }
}

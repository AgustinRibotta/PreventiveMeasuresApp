package com.cosatcal.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainView extends BorderPane {

  private final InputPanel inputPanel = new InputPanel();
  private final ResultPanel resultPanel = new ResultPanel();
  private final Button reloadButton = new Button("Ricarica dati");

  private double dragOffsetX;
  private double dragOffsetY;

  public MainView() {
    getStyleClass().add("root-pane");
    setTop(buildHeader());

    HBox body = new HBox(10, inputPanel, resultPanel);
    HBox.setHgrow(resultPanel, Priority.ALWAYS);
    body.setPadding(new Insets(10));
    setCenter(body);

    // Temporary: remove when the controller feeds real data
    // inputPanel.showSampleData();
    // resultPanel.showSampleData();
  }

  private HBox buildHeader() {
    Label title = new Label("CosatCal");
    title.getStyleClass().add("app-title");

    reloadButton.getStyleClass().add("ghost-button");

    Button minimizeButton = new Button("–");
    minimizeButton.getStyleClass().add("window-button");
    minimizeButton.setOnAction(e -> ((Stage) getScene().getWindow()).setIconified(true));

    Button closeButton = new Button("✕");
    closeButton.getStyleClass().addAll("window-button", "close-button");
    closeButton.setOnAction(e -> Platform.exit());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox header = new HBox(8, title, spacer, reloadButton, minimizeButton, closeButton);
    header.getStyleClass().add("header");
    header.setAlignment(Pos.CENTER_LEFT);

    // Arrastrar la ventana desde el header
    header.setOnMousePressed(e -> {
      dragOffsetX = e.getSceneX();
      dragOffsetY = e.getSceneY();
    });
    header.setOnMouseDragged(e -> {
      Window window = getScene().getWindow();
      window.setX(e.getScreenX() - dragOffsetX);
      window.setY(e.getScreenY() - dragOffsetY);
    });

    return header;
  }

  public InputPanel inputPanel() {
    return inputPanel;
  }

  public ResultPanel resultPanel() {
    return resultPanel;
  }

  public Button reloadButton() {
    return reloadButton;
  }
}

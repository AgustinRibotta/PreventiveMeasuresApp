package com.cosatcal.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

  }

  private HBox buildHeader() {
    Label title = new Label("CosatCal");
    title.getStyleClass().add("app-title");

    reloadButton.getStyleClass().add("ghost-button");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox header = new HBox(8, title, spacer, reloadButton);
    header.getStyleClass().add("header");
    header.setAlignment(Pos.CENTER_LEFT);

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

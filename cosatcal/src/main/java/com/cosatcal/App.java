package com.cosatcal;

import com.cosatcal.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

  @Override
  public void start(Stage stage) {
    Scene scene = new Scene(new MainView(), 1100, 640);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    stage.setTitle("CosatCal");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}

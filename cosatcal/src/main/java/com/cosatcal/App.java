package com.cosatcal;

import com.cosatcal.controller.MainController;
import com.cosatcal.repository.ExcelRepository;
import com.cosatcal.view.MainView;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

  @Override
  public void start(Stage stage) {
    MainView view = new MainView();

    Scene scene = new Scene(view, 825, 546);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    stage.setTitle("CosatCal");
    stage.setScene(scene);
    stage.show();

    // Created after show() so an error on the first load can display its alert
    Path file = Path.of("CosatCal_dati.xlsx");
    new MainController(view, new ExcelRepository(file), file.getFileName().toString());
  }

  public static void main(String[] args) {
    launch();
  }
}

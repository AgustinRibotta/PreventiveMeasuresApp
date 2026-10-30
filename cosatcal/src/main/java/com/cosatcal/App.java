package com.cosatcal;

import com.cosatcal.controller.MainController;
import com.cosatcal.repository.ExcelRepository;
import com.cosatcal.view.MainView;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

  private static final String EXCEL_NAME = "CosatCal_dati.xlsx";

  @Override
  public void start(Stage stage) {
    MainView view = new MainView();

    Scene scene = new Scene(view, 825, 546);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    stage.setTitle("CosatCal");
    stage.setScene(scene);
    stage.show();

    new MainController(view, new ExcelRepository(excelPath()), EXCEL_NAME);
  }

  private static Path excelPath() {
    String appPath = System.getProperty("jpackage.app-path");
    Path folder = appPath != null ? Path.of(appPath).getParent() : Path.of(".");
    return folder.resolve(EXCEL_NAME);
  }

  public static void main(String[] args) {
    launch();
  }
}

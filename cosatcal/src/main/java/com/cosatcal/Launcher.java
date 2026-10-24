package com.cosatcal;

/**
 * Entry point for the packaged app.
 * It does not extend Application, so the JVM does not look for the JavaFX
 * modules at startup and the single jar works outside Maven.
 */
public class Launcher {

  public static void main(String[] args) {
    App.main(args);
  }
}

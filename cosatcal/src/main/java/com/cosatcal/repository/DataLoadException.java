package com.cosatcal.repository;

/**
 * DataLoadException
 */
public class DataLoadException extends RuntimeException {

  public DataLoadException(String message) {
    super(message);
  }

  public DataLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}

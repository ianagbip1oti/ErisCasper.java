package com.github.princesslana.eriscasper;

public class ErisCasperFatalException extends RuntimeException {
  public ErisCasperFatalException() {
    super();
  }

  public ErisCasperFatalException(String msg) {
    super(msg);
  }

  public ErisCasperFatalException(Throwable cause) {
    super(cause);
  }

  public ErisCasperFatalException(String msg, Throwable cause) {
    super(msg, cause);
  }
}

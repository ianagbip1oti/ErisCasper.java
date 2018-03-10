package com.github.princesslana.eriscasper.api;

public final class Routes {
  private Routes() { }
  
  public static Route<Void, Void> getGateway() {
    return Route.get("/gateway", null);
  }
  
}

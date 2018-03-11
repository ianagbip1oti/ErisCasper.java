package com.github.princesslana.eriscasper.api;

public final class RouteCatalog {

  private RouteCatalog() {}

  public static Route<Void, String> getGateway() {
    return Route.get("/gateway", String.class);
  }
}

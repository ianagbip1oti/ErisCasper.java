package com.github.princesslana.eriscasper.api;

public final class RouteCatalog {

  private RouteCatalog() {}

  public static Route<Void, GatewayResponse> getGateway() {
    return Route.get("/gateway", GatewayResponse.class);
  }
}

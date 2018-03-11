package com.github.princesslana.eriscasper.rest;

public final class RouteCatalog {

  private RouteCatalog() {}

  public static Route<Void, GatewayResponse> getGateway() {
    return Route.get("/gateway", GatewayResponse.class);
  }
}

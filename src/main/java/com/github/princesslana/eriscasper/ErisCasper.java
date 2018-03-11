package com.github.princesslana.eriscasper;

import com.github.princesslana.eriscasper.api.GatewayResponse;
import com.github.princesslana.eriscasper.api.Route;
import com.github.princesslana.eriscasper.api.RouteCatalog;
import io.reactivex.Flowable;

public class ErisCasper {

  public Flowable<GatewayResponse> events() {
    return Route.execute(RouteCatalog.getGateway()).toFlowable();
  }

  public static ErisCasper create() {
    return new ErisCasper();
  }
}

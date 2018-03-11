package com.github.princesslana.eriscasper;

import com.github.princesslana.eriscasper.api.Route;
import com.github.princesslana.eriscasper.api.RouteCatalog;
import io.reactivex.Flowable;
import okhttp3.Response;

public class ErisCasper {

  public Flowable<Response> events() {
    return Route.execute(RouteCatalog.getGateway()).toFlowable();
  }

  public static ErisCasper create() {
    return new ErisCasper();
  }
}

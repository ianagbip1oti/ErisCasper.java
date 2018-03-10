package com.github.princesslana.eriscasper;

import io.reactivex.Flowable;

import com.github.princesslana.eriscasper.api.Route;
import com.github.princesslana.eriscasper.api.Routes;

public class ErisCasper {
  
  public Flowable<Event> events() {
    return Route.execute(Routes.getGateway()).toFlowable();
  }
  
  public static ErisCasper create() {
    return new ErisCasper();
  }
}

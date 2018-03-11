package com.github.princesslana.eriscasper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.gateway.Gateway;
import com.github.princesslana.eriscasper.gateway.Payload;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.Routes;
import com.github.princesslana.eriscasper.util.Jackson;
import io.reactivex.Flowable;
import java.io.IOException;
import okhttp3.OkHttpClient;

public class ErisCasper {

  public Flowable<Payload> events() {
    OkHttpClient httpClient = new OkHttpClient();

    ObjectMapper jackson = Jackson.newObjectMapper();

    try (Gateway gateway = new Gateway(httpClient, jackson)) {
      return new Routes(httpClient, jackson)
          .execute(RouteCatalog.getGateway())
          .toFlowable()
          .flatMap(gr -> gateway.connect(gr.getUrl()))
          .onBackpressureBuffer();
    } catch (IOException e) {
      return Flowable.error(e);
    }
  }

  public static ErisCasper create() {
    return new ErisCasper();
  }
}

package com.github.princesslana.eriscasper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.gateway.Gateway;
import com.github.princesslana.eriscasper.gateway.Payloads;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.Routes;
import com.github.princesslana.eriscasper.util.Jackson;
import io.reactivex.Flowable;
import java.io.IOException;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErisCasper {

  private static final Logger LOG = LoggerFactory.getLogger(ErisCasper.class);

  private final String token;

  private ErisCasper(String token) {
    this.token = token;
  }

  public Flowable<Event> events() {
    OkHttpClient httpClient = new OkHttpClient();
    ObjectMapper jackson = Jackson.newObjectMapper();
    Payloads payloads = new Payloads(jackson);

    try (Gateway gateway = new Gateway(httpClient, payloads)) {
      return new Routes(httpClient, jackson)
          .execute(RouteCatalog.getGateway())
          .toFlowable()
          .flatMap(gr -> gateway.connect(gr.getUrl(), token))
          .flatMapMaybe(payloads::toEvent)
          .onBackpressureBuffer();
    } catch (IOException e) {
      return Flowable.error(e);
    }
  }

  public static ErisCasper create() {
    return create(System.getenv("EC_TOKEN"));
  }

  public static ErisCasper create(String token) {
    return new ErisCasper(token);
  }
}

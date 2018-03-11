package com.github.princesslana.eriscasper.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routes {

  private static final Logger LOG = LoggerFactory.getLogger(Routes.class);

  private final OkHttpClient client;
  private final ObjectMapper jackson;

  public Routes(OkHttpClient client, ObjectMapper jackson) {
    this.client = client;
    this.jackson = jackson;
  }

  public <Rs> Single<Rs> execute(Route<Void, Rs> route) {
    return Single.fromCallable(
            () -> {
              LOG.debug("Executing: {}...", route);

              Request rq =
                  new Request.Builder()
                      .method(route.getMethod().get(), null)
                      .url(route.getUrl())
                      .build();

              try (Response rs = client.newCall(rq).execute()) {
                return jackson.readValue(rs.body().byteStream(), route.getResponseClass());
              }
            })
        .doOnSuccess(r -> LOG.debug("Done: {} -> {}.", route, r))
        .doOnError(e -> LOG.warn("Error: {} - {}.", route, e));
  }

  public <Rq, Rs> Single<Rs> execute(Route<Rq, Rs> route, Rq rq) {
    throw new UnsupportedOperationException();
  }
}

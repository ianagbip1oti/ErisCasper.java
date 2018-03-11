package com.github.princesslana.eriscasper.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
public abstract class Route<Rq, Rs> {

  private static final Logger LOG = LoggerFactory.getLogger(Route.class);

  private static final String VERSION = "v6";
  private static final String URL = String.format("https://discordapp.com/api/%s", VERSION);

  private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
  private static final ObjectMapper JACKSON = new ObjectMapper();

  protected static enum Method {
    GET("GET");

    private final String method;

    private Method(String method) {
      this.method = method;
    }

    public String get() {
      return method;
    }
  }

  protected abstract Method getMethod();

  protected abstract String getPath();

  protected abstract Class<Rq> getRequestClass();

  protected abstract Class<Rs> getResponseClass();

  private String getUrl() {
    return String.format("%s%s", URL, getPath());
  }

  public static <Rs> Route<Void, Rs> get(String path, Class<Rs> rsClass) {
    return ImmutableRoute.<Void, Rs>builder()
        .method(Method.GET)
        .path(path)
        .requestClass(Void.class)
        .responseClass(rsClass)
        .build();
  }

  public static <Rs> Single<Rs> execute(Route<Void, Rs> route) {
    return Single.fromCallable(
            () -> {
              LOG.debug("Executing: {}...", route);

              Request rq =
                  new Request.Builder()
                      .method(route.getMethod().get(), null)
                      .url(route.getUrl())
                      .build();

              try (Response rs = HTTP_CLIENT.newCall(rq).execute()) {
                return JACKSON.readValue(rs.body().byteStream(), route.getResponseClass());
              }
            })
        .doOnSuccess(r -> LOG.debug("Done: {} -> {}.", route, r))
        .doOnError(e -> LOG.warn("Error: {} - {}.", route, e));
  }

  public static <Rq, Rs> Single<Rs> execute(Route<Rq, Rs> route, Rq rq) {
    throw new UnsupportedOperationException();
  }
}

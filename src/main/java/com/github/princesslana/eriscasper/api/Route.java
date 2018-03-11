package com.github.princesslana.eriscasper.api;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.immutables.value.Value;

@Value.Immutable
public abstract class Route<Rq, Rs> {

  private static final String VERSION = "v6";
  private static final String URL = String.format("https://discordapp.com/api/%s", VERSION);

  private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

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

  public static <Rs> Single<Response> execute(Route<Void, Rs> route) {
    return Single.fromCallable(
        () -> {
          Request request =
              new Request.Builder()
                  .method(route.getMethod().get(), null)
                  .url(route.getUrl())
                  .build();

          return HTTP_CLIENT.newCall(request).execute();
        });
  }

  public static <Rq, Rs> Single<Rs> execute(Route<Rq, Rs> route, Rq rq) {
    throw new UnsupportedOperationException();
  }
}

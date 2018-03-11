package com.github.princesslana.eriscasper.rest;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Route<Rq, Rs> {

  private static final String VERSION = "v6";
  private static final String URL = String.format("https://discordapp.com/api/%s", VERSION);

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

  public String getUrl() {
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
}

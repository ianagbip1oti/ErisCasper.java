package com.github.princesslana.eriscasper.rest;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Route<Rq, Rs> {

  private static final String VERSION = "v6";
  private static final String URL = String.format("https://discordapp.com/api/%s", VERSION);

  protected static enum HttpMethod {
    DELETE("DELETE"),
    GET("GET"),
    POST("POST"),
    PUT("PUT");

    private final String method;

    private HttpMethod(String method) {
      this.method = method;
    }

    public String get() {
      return method;
    }
  }

  protected abstract HttpMethod getMethod();

  public abstract String getPath();

  protected abstract Class<Rq> getRequestClass();

  protected abstract Class<Rs> getResponseClass();

  public String getUrl() {
    return String.format("%s%s", URL, getPath());
  }

  public static <Rs> Route<Void, Rs> delete(String path, Class<Rs> rsClass) {
    return ImmutableRoute.<Void, Rs>builder()
        .method(HttpMethod.DELETE)
        .path(path)
        .requestClass(Void.class)
        .responseClass(rsClass)
        .build();
  }

  public static <Rs> Route<Void, Rs> get(String path, Class<Rs> rsClass) {
    return ImmutableRoute.<Void, Rs>builder()
        .method(HttpMethod.GET)
        .path(path)
        .requestClass(Void.class)
        .responseClass(rsClass)
        .build();
  }

  public static <Rq, Rs> Route<Rq, Rs> post(String path, Class<Rq> rqClass, Class<Rs> rsClass) {
    return ImmutableRoute.<Rq, Rs>builder()
        .method(HttpMethod.POST)
        .path(path)
        .requestClass(rqClass)
        .responseClass(rsClass)
        .build();
  }

  public static <Rq, Rs> Route<Rq, Rs> put(String path, Class<Rq> rqClass, Class<Rs> rsClass) {
    return ImmutableRoute.<Rq, Rs>builder()
        .method(HttpMethod.PUT)
        .path(path)
        .requestClass(rqClass)
        .responseClass(rsClass)
        .build();
  }
}

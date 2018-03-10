package com.github.princesslana.eriscasper.api;

import java.nio.ByteBuffer;

import org.immutables.value.Value;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.Single;
import io.reactivex.netty.protocol.http.client.HttpClient;

@Value.Immutable
public interface Route<Request, Response> {
  HttpMethod getMethod();
  
  String getPath();
  
  Class<Request> getRequestClass();
  
  Class<Response> getResponseClass();
  
  public static <Rs> Route<Void, Rs> get(String path, Class<Rs> rsClass) {
    return ImmutableRoute.builder().method(HttpMethod.GET).path(path).requestClass(Void.class)
      .responseClass(rsClass).build();
  }
  
  public static <Rs> Single<ByteBuf> execute(Route<Void, Rs> route) {
    return HttpClient.newClient(Rest.HOST, 443)
                  .createRequest(route.getMethod(), String.format("/api/%s%s", Rest.VERSION, route.getPath()))
                  .flatMap(r -> r.getContent())
                  .toSingle();
  }
  
  public static <Rq, Rs> Single<Rs> execute(Route<Rq, Rs> route, Rq rq) {
    throw new UnsupportedOperationException();
  }
}

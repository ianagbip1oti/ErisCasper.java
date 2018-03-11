package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gateway implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(Gateway.class);

  private final OkHttpClient client;
  private final ObjectMapper jackson;

  private WebSocket ws;

  public Gateway(OkHttpClient client, ObjectMapper jackson) {
    this.client = client;
    this.jackson = jackson;
  }

  public Flowable<Payload> connect(String url) {
    return Flowable.create(
        em -> {
          Request request = new Request.Builder().url(url).build();

          ws =
              client.newWebSocket(
                  request,
                  new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                      LOG.info("Open: {}.", response);
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                      LOG.info("Message: {}", text);
                      try {
                        em.onNext(jackson.readValue(text, Payload.class));
                      } catch (IOException e) {
                        LOG.info("Message Failed: {}", e);
                        em.onError(e);
                      }
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                      LOG.info("Failure: {}/{}. Reconnecting...", t, response);
                      ws = client.newWebSocket(request, this);
                    }
                  });
        },
        BackpressureStrategy.BUFFER);
  }

  @Override
  public void close() {
    if (ws != null) {
      ws.close(1000, "Closing");
    }
  }
}

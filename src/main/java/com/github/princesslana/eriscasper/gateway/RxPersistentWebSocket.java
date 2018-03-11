package com.github.princesslana.eriscasper.gateway;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WebSocket that will attempted to maintain a connection and emits messages in a reactive manner.
 *
 * <p>This means it will attempted to reconnect on disconnections.
 *
 * <p>Currently only String messages are supported.
 */
public class RxPersistentWebSocket implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(RxPersistentWebSocket.class);

  private final OkHttpClient client;

  private WebSocket ws;

  public RxPersistentWebSocket(OkHttpClient client) {
    this.client = client;
  }

  public Flowable<Message<?>> connect(String url) {

    return Flowable.create(
        em -> {
          Request request = new Request.Builder().url(url).build();

          ws =
              client.newWebSocket(
                  request,
                  new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                      LOG.debug("Open: {}.", response);
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                      LOG.debug("Message: {}.", text);
                      em.onNext(ImmutableStringMessage.of(text));
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

  public void send(String payload) {
    LOG.debug("Sending: {}...", payload);
    ws.send(payload);
  }

  @Override
  public void close() {
    if (ws != null) {
      ws.close(1000, "Closing");
    }
  }

  public static interface Message<T> {
    @Value.Parameter
    T getContent();
  }

  @Value.Immutable
  public static interface StringMessage extends Message<String> {}
}

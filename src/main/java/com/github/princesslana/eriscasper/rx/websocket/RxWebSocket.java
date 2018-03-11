package com.github.princesslana.eriscasper.rx.websocket;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import java.io.Closeable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RxWebSocket implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(RxWebSocket.class);

  private final OkHttpClient http;

  private WebSocket ws;

  public RxWebSocket(OkHttpClient http) {
    this.http = http;
  }

  public Flowable<RxWebSocketEvent> connect(String url) {
    return Flowable.<RxWebSocketEvent>create(
            em -> {
              Request rq = new Request.Builder().url(url).build();

              ws = http.newWebSocket(rq, new Listener(em));
            },
            BackpressureStrategy.BUFFER)
        .doOnNext(e -> LOG.debug("Received: {}.", e))
        .doOnError(e -> LOG.warn("Error: {}.", e));
  }

  public Completable send(String text) {
    return Completable.fromAction(() -> ws.send(text))
        .doOnComplete(() -> LOG.debug("Sent: {}.", text));
  }

  @Override
  public void close() {
    if (ws != null) {
      ws.close(1000, "Closed.");
    }
  }

  private static class Listener extends WebSocketListener {
    private final FlowableEmitter<RxWebSocketEvent> em;

    public Listener(FlowableEmitter<RxWebSocketEvent> em) {
      this.em = em;
    }

    @Override
    public void onClosed(WebSocket ws, int code, String reason) {
      em.onNext(ClosedTuple.of(ws, code, reason));
    }

    @Override
    public void onClosing(WebSocket ws, int code, String reason) {
      em.onNext(ClosingTuple.of(ws, code, reason));
    }

    @Override
    public void onFailure(WebSocket ws, Throwable t, Response response) {
      em.onNext(FailureTuple.of(ws, t, response));
      em.onError(t);
    }

    @Override
    public void onMessage(WebSocket ws, okio.ByteString bytes) {
      em.onNext(ByteStringMessageTuple.of(ws, bytes));
    }

    @Override
    public void onMessage(WebSocket ws, String text) {
      em.onNext(StringMessageTuple.of(ws, text));
    }

    @Override
    public void onOpen(WebSocket ws, Response response) {
      em.onNext(OpenTuple.of(ws, response));
    }
  }
}

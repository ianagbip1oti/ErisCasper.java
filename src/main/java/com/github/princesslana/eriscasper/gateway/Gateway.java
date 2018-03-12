package com.github.princesslana.eriscasper.gateway;

import com.github.princesslana.eriscasper.rx.websocket.RxWebSocket;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocketEvent;
import com.google.common.io.Closer;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gateway implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(Gateway.class);

  private static final String VERSION = "6";
  private static final String ENCODING = "json";

  private final OkHttpClient client;
  private final Payloads payloads;

  private final Closer closer = Closer.create();

  public Gateway(OkHttpClient client, Payloads payloads) {
    this.client = client;
    this.payloads = payloads;
  }

  public Flowable<Payload> connect(String url, String token) {
    RxWebSocket ws = closer.register(new RxWebSocket(client));

    Flowable<Payload> ps =
        ws.connect(String.format("%s?v=%s&encoding=%s", url, VERSION, ENCODING))
            .ofType(RxWebSocketEvent.StringMessage.class)
            .map(RxWebSocketEvent.StringMessage::getText)
            .flatMapSingle(payloads::read)
            .cache();

    ps.filter(Payload.isOp(OpCode.HELLO)).subscribe(p -> setupHeartbeat(ws, p));

    ps.filter(Payload.isOp(OpCode.HELLO)).flatMapCompletable(p -> identify(ws, token)).subscribe();

    return ps;
  }

  private Completable send(RxWebSocket ws, Payload payload) {
    return payloads.writeToString(payload).flatMapCompletable(ws::send);
  }

  private Completable identify(RxWebSocket ws, String token) {
    return send(ws, payloads.identify(token));
  }

  private void setupHeartbeat(RxWebSocket ws, Payload hello) {
    payloads
        .dataAs(hello, Payloads.Heartbeat.class)
        .subscribe(
            h -> {
              Observable.interval(h.getHeartbeatInterval(), TimeUnit.MILLISECONDS)
                  .flatMapCompletable(
                      l -> send(ws, ImmutablePayload.builder().op(OpCode.HEARTBEAT).build()))
                  .subscribe();
            });
  }

  @Override
  public void close() throws IOException {
    closer.close();
  }
}

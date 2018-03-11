package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocket;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocketEvent;
import com.google.common.io.Closer;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
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
  private final ObjectMapper jackson;

  private final Closer closer = Closer.create();

  public Gateway(OkHttpClient client, ObjectMapper jackson) {
    this.client = client;
    this.jackson = jackson;
  }

  public Flowable<Payload> connect(String url) {
    RxWebSocket ws = closer.register(new RxWebSocket(client));

    Flowable<Payload> payloads =
        ws.connect(String.format("%s?v=%s&encoding=%s", url, VERSION, ENCODING))
            .ofType(RxWebSocketEvent.StringMessage.class)
            .map(m -> jackson.readValue(m.getText(), Payload.class))
            .cache();

    payloads.filter(p -> p.op() == 10).subscribe(p -> setupHeartbeat(ws, p));

    return payloads;
  }

  private Completable send(RxWebSocket ws, Payload payload) {
    return Single.fromCallable(() -> jackson.writeValueAsString(payload))
        .flatMapCompletable(ws::send);
  }

  private void setupHeartbeat(RxWebSocket ws, Payload hello) {
    hello
        .d()
        .ifPresent(
            json -> {
              long heartbeatInterval = json.get("heartbeat_interval").asLong();

              Observable.interval(heartbeatInterval, TimeUnit.MILLISECONDS)
                  .flatMapCompletable((l) -> send(ws, ImmutablePayload.builder().op(1).build()))
                  .subscribe();
            });
  }

  @Override
  public void close() throws IOException {
    closer.close();
  }
}

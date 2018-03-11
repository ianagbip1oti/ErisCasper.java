package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.gateway.RxPersistentWebSocket.StringMessage;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gateway implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(Gateway.class);

  private static final String VERSION = "6";
  private static final String ENCODING = "json";

  private final OkHttpClient client;
  private final ObjectMapper jackson;

  private RxPersistentWebSocket ws;

  public Gateway(OkHttpClient client, ObjectMapper jackson) {
    this.client = client;
    this.jackson = jackson;
  }

  public Flowable<Payload> connect(String url) {
    ws = new RxPersistentWebSocket(client);

    Flowable<Payload> payloads =
        ws.connect(String.format("%s?v=%s&encoding=%s", url, VERSION, ENCODING))
            .ofType(StringMessage.class)
            .map(m -> jackson.readValue(m.getContent(), Payload.class))
            .cache();

    payloads.filter(p -> p.op() == 10).subscribe(this::setupHeartbeat);

    return payloads;
  }

  private void setupHeartbeat(Payload hello) {
    hello
        .d()
        .ifPresent(
            json -> {
              long heartbeatInterval = json.get("heartbeat_interval").asLong();

              Observable.interval(heartbeatInterval, TimeUnit.MILLISECONDS)
                  .subscribe(i -> ws.send("{ \"op\" : 1, \"d\" : null }"));
            });
  }

  @Override
  public void close() {
    if (ws != null) {
      ws.close();
    }
  }
}

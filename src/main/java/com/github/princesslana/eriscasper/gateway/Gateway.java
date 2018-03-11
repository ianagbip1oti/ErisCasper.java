package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.gateway.RxPersistentWebSocket.Message;
import com.github.princesslana.eriscasper.gateway.RxPersistentWebSocket.StringMessage;
import io.reactivex.Flowable;
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

    Flowable<Message<?>> messages =
        ws.connect(String.format("%s?v=%s&encoding=%s", url, VERSION, ENCODING));

    Flowable<Payload> payloads =
        messages
            .ofType(StringMessage.class)
            .map(m -> jackson.readValue(m.getContent(), Payload.class));

    return payloads;
  }

  @Override
  public void close() {
    if (ws != null) {
      ws.close();
    }
  }
}

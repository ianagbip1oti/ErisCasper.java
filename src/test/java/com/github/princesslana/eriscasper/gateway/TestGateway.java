package com.github.princesslana.eriscasper.gateway;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.notNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.BotToken;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.faker.DiscordFaker;
import com.github.princesslana.eriscasper.gateway.Payloads.ConnectionProperties;
import com.github.princesslana.eriscasper.gateway.Payloads.Identify;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocket;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocketEvent;
import com.github.princesslana.eriscasper.rx.websocket.StringMessageTuple;
import com.github.princesslana.eriscasper.util.Jackson;
import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import okhttp3.WebSocket;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGateway {

  @Mock private RxWebSocket mockRxWebSocket;
  @Mock private WebSocket mockWebSocket;

  private final ObjectMapper jackson = Jackson.newObjectMapper();
  private final Payloads payloads = new Payloads(jackson);

  private Gateway subject;

  @BeforeMethod
  public void mockito() {
    MockitoAnnotations.initMocks(this);
  }

  @BeforeMethod
  public void subject() {
    subject = new Gateway(mockRxWebSocket, payloads);
  }

  @Test
  public void connect_shouldConnectWithVersionAndJsonEncoding() {
    PublishSubject<RxWebSocketEvent> wsEvents = PublishSubject.create();

    ArgumentCaptor<String> urlCapture = ArgumentCaptor.forClass(String.class);
    given(mockRxWebSocket.connect(urlCapture.capture())).willReturn(wsEvents);

    subject.connect("wss://localhost", DiscordFaker.botToken());

    Assertions.assertThat(urlCapture.getValue()).isEqualTo("wss://localhost?v=6&encoding=json");
  }

  @Test
  public void connect_whenHelloPayload_shouldIdentify() throws IOException {
    PublishSubject<RxWebSocketEvent> wsEvents = PublishSubject.create();

    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

    given(mockRxWebSocket.connect(notNull())).willReturn(wsEvents);
    given(mockRxWebSocket.send(message.capture())).willReturn(Completable.complete());

    BotToken token = DiscordFaker.botToken();

    TestObserver<Event> subscriber = subject.connect("wss://localhost", token).test();

    JsonNode d =
        jackson.valueToTree(ImmutableHeartbeat.builder().heartbeatInterval(Long.MAX_VALUE).build());

    wsEvents.onNext(
        StringMessageTuple.of(
            mockWebSocket,
            jackson.writeValueAsString(ImmutablePayload.builder().op(OpCode.HELLO).d(d).build())));

    String sent = message.getValue();

    Payload p = jackson.readValue(sent, Payload.class);
    Assertions.assertThat(p).hasFieldOrPropertyWithValue("op", OpCode.IDENTIFY);

    Identify identify = payloads.dataAs(p, Identify.class).blockingGet();

    Assertions.assertThat(identify)
        .hasFieldOrPropertyWithValue("token", token)
        .hasFieldOrPropertyWithValue("properties", ConnectionProperties.ofDefault());
  }
}

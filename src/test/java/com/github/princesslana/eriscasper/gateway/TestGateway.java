package com.github.princesslana.eriscasper.gateway;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.notNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.BotToken;
import com.github.princesslana.eriscasper.data.event.Event;
import com.github.princesslana.eriscasper.data.event.HelloEvent;
import com.github.princesslana.eriscasper.data.event.ImmutableHelloEventData;
import com.github.princesslana.eriscasper.data.event.ReadyEvent;
import com.github.princesslana.eriscasper.data.event.ReadyEventData;
import com.github.princesslana.eriscasper.event.EventType;
import com.github.princesslana.eriscasper.faker.DataFaker;
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

  private final PublishSubject<RxWebSocketEvent> wsEvents = PublishSubject.create();

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

    given(mockRxWebSocket.connect(notNull())).willReturn(wsEvents);
  }

  @Test
  public void connect_shouldConnectWithVersionAndJsonEncoding() {
    ArgumentCaptor<String> urlCapture = ArgumentCaptor.forClass(String.class);
    given(mockRxWebSocket.connect(urlCapture.capture())).willReturn(wsEvents);

    connect();

    Assertions.assertThat(urlCapture.getValue()).isEqualTo("wss://localhost?v=6&encoding=json");
  }

  @Test
  public void connect_whenInvalidPayload_shouldNotComplete() {
    TestObserver<Event> subscriber = connect();

    wsEvents.onNext(StringMessageTuple.of(mockWebSocket, "bad_payload_data"));

    subscriber.assertNotComplete();
    subscriber.assertNoValues();
  }

  @Test
  public void connect_whenHelloPayload_shouldIdentify() throws IOException {
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

    given(mockRxWebSocket.send(message.capture())).willReturn(Completable.complete());

    BotToken token = DiscordFaker.botToken();

    TestObserver<Event> subscriber = connect(token);

    JsonNode d =
        jackson.valueToTree(
            HelloEvent.of(
                ImmutableHelloEventData.builder().heartbeatInterval(Long.MAX_VALUE).build()));

    wsEvents.onNext(stringMessageOf(ImmutablePayload.builder().op(OpCode.HELLO).d(d).build()));

    String sent = message.getValue();

    Payload p = jackson.readValue(sent, Payload.class);
    Assertions.assertThat(p).hasFieldOrPropertyWithValue("op", OpCode.IDENTIFY);

    Identify identify = payloads.dataAs(p, Identify.class).blockingGet();

    Assertions.assertThat(identify)
        .hasFieldOrPropertyWithValue("token", token)
        .hasFieldOrPropertyWithValue("properties", ConnectionProperties.ofDefault());
  }

  @Test
  public void connect_whenReadyPayload_shouldEmitReadyEvent() {
    TestObserver<Event> subscriber = connect();

    ReadyEventData ready = DataFaker.ready();

    wsEvents.onNext(
        stringMessageOf(
            ImmutablePayload.builder()
                .op(OpCode.DISPATCH)
                .d(jackson.valueToTree(ready))
                .t(EventType.READY)
                .build()));

    subscriber.assertNotComplete();
    subscriber.assertValues(ReadyEvent.of(ready));
  }

  @Test
  public void connect_whenBadEventType_shouldNotComplete() {
    TestObserver<Event> subscriber = connect();

    wsEvents.onNext(StringMessageTuple.of(mockWebSocket, "{\"op\":0,\"t\":\"BAD_EVENT_TYPE\"}"));

    subscriber.assertNotComplete();
    subscriber.assertNoValues();
  }

  @Test
  public void connect_whenBadEventData_shouldNotComplete() {
    TestObserver<Event> subscriber = connect();

    wsEvents.onNext(
        StringMessageTuple.of(
            mockWebSocket, "{\"op\":0,\"d\":\"bad_event_data\",\"t\":\"READY\"}"));

    subscriber.assertNotComplete();
    subscriber.assertNoValues();
  }

  private TestObserver<Event> connect() {
    return connect(DiscordFaker.botToken());
  }

  private TestObserver<Event> connect(BotToken token) {
    return subject.connect("wss://localhost", token).test();
  }

  private RxWebSocketEvent.StringMessage stringMessageOf(Payload payload) {
    try {
      return StringMessageTuple.of(mockWebSocket, jackson.writeValueAsString(payload));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

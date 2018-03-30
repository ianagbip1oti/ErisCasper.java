package com.github.princesslana.eriscasper.rx.websocket;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.notNull;
import static org.mockito.BDDMockito.then;

import com.google.common.collect.ImmutableMap;
import io.reactivex.observers.TestObserver;
import java.util.Optional;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestRxWebSocket {

  private static final String TEST_MESSAGE = "test message";
  private static final String TEST_WS_URL = "wss://test.url/";

  @Mock private OkHttpClient mockClient;

  @Mock private WebSocket mockWebSocket;

  private ArgumentCaptor<WebSocketListener> listener;

  private RxWebSocket subject;

  @BeforeMethod
  public void mockito() {
    MockitoAnnotations.initMocks(this);
  }

  @BeforeMethod
  public void setupSubject() {
    MockitoAnnotations.initMocks(this);

    subject = new RxWebSocket(mockClient);

    listener = ArgumentCaptor.forClass(WebSocketListener.class);

    given(mockClient.newWebSocket(notNull(), listener.capture())).willReturn(mockWebSocket);
  }

  @Test
  public void connect_shouldConnectToUrl() {
    ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);

    given(mockClient.newWebSocket(request.capture(), notNull())).willReturn(null);

    subject.connect(TEST_WS_URL).subscribe();

    Assertions.assertThat(request.getValue().url().toString()).isEqualTo("https://test.url/");
  }

  @Test
  public void connect_whenListenerCalled_shouldEmitEvent() {
    Response mockResponse = Mockito.mock(Response.class);
    Throwable throwable = new NullPointerException();

    ImmutableMap.<Consumer<WebSocketListener>, RxWebSocketEvent>builder()
        .put(
            l -> l.onClosed(mockWebSocket, 0, "closed"), ClosedTuple.of(mockWebSocket, 0, "closed"))
        .put(
            l -> l.onClosing(mockWebSocket, 0, "closing"),
            ClosingTuple.of(mockWebSocket, 0, "closing"))
        .put(
            l -> l.onFailure(mockWebSocket, throwable, mockResponse),
            FailureTuple.of(mockWebSocket, throwable, Optional.of(mockResponse)))
        .put(
            l -> l.onMessage(mockWebSocket, ByteString.encodeUtf8(TEST_MESSAGE)),
            ByteStringMessageTuple.of(mockWebSocket, ByteString.encodeUtf8(TEST_MESSAGE)))
        .put(
            l -> l.onMessage(mockWebSocket, TEST_MESSAGE),
            StringMessageTuple.of(mockWebSocket, TEST_MESSAGE))
        .put(l -> l.onOpen(mockWebSocket, mockResponse), OpenTuple.of(mockWebSocket, mockResponse))
        .build()
        .forEach(
            (k, v) -> {
              TestObserver<RxWebSocketEvent> subscriber = subject.connect(TEST_WS_URL).test();
              k.accept(listener.getValue());
              subscriber.assertValues(v);
            });
  }

  @Test
  public void connect_whenOnFailure_shouldError() {
    TestObserver<RxWebSocketEvent> subscriber = subject.connect(TEST_WS_URL).test();
    listener.getValue().onFailure(mockWebSocket, new NullPointerException(), null);
    subscriber.assertError(NullPointerException.class);
  }

  @Test
  public void send_whenBeforeConnect_shouldThrowNullPointerException() {
    subject.send(TEST_MESSAGE).test().assertError(NullPointerException.class);
  }

  @Test
  public void send_whenStringMessage_shouldSendToWebSocket() {
    subject.connect(TEST_WS_URL).subscribe();
    TestObserver<Void> subscriber = subject.send(TEST_MESSAGE).test();

    subscriber.assertComplete();
    then(mockWebSocket).should().send(TEST_MESSAGE);
  }
}

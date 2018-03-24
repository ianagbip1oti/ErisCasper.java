package com.github.princesslana.eriscasper.rx.websocket;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.notNull;

import io.reactivex.observers.TestObserver;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestRxWebSocket {

  private static final String TEST_WS_URL = "wss://test.url/";

  @Mock private OkHttpClient mockClient;

  @Mock private WebSocket mockWebSocket;

  private RxWebSocket subject;

  @BeforeMethod
  public void mockito() {
    MockitoAnnotations.initMocks(this);
  }

  @BeforeMethod
  public void setupSubject() {
    subject = new RxWebSocket(mockClient);
  }

  @Test
  public void connect_connectsToUrl() {
    ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);

    given(mockClient.newWebSocket(request.capture(), notNull())).willReturn(null);

    subject.connect(TEST_WS_URL).subscribe();

    Assertions.assertThat(request.getValue().url().toString()).isEqualTo("https://test.url/");
  }

  @Test
  public void connect_whenStringMessage_emitsStringMessageEvent() {
    ArgumentCaptor<WebSocketListener> wsl = ArgumentCaptor.forClass(WebSocketListener.class);

    TestObserver<RxWebSocketEvent> subscriber = new TestObserver<>();

    given(mockClient.newWebSocket(notNull(), wsl.capture())).willReturn(mockWebSocket);

    subject.connect(TEST_WS_URL).subscribe(subscriber);

    wsl.getValue().onMessage(mockWebSocket, "test message");

    subscriber.assertValuesOnly(StringMessageTuple.of(mockWebSocket, "test message"));
  }
}

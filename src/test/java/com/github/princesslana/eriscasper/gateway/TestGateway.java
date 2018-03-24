package com.github.princesslana.eriscasper.gateway;

import static org.mockito.BDDMockito.given;

import com.github.princesslana.eriscasper.rx.websocket.RxWebSocket;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocketEvent;
import io.reactivex.subjects.PublishSubject;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGateway {

  @Mock private RxWebSocket mockWs;

  @Mock private Payloads mockPayloads;

  private Gateway subject;

  @BeforeMethod
  public void mockito() {
    MockitoAnnotations.initMocks(this);
  }

  @BeforeMethod
  public void subject() {
    subject = new Gateway(mockWs, mockPayloads);
  }

  @Test
  public void connect_shouldConnectWithVersionAndJsonEncoding() {
    PublishSubject<RxWebSocketEvent> wsEvents = PublishSubject.create();

    ArgumentCaptor<String> urlCapture = ArgumentCaptor.forClass(String.class);
    given(mockWs.connect(urlCapture.capture())).willReturn(wsEvents);

    subject.connect("wss://localhost", null);

    Assertions.assertThat(urlCapture.getValue()).isEqualTo("wss://localhost?v=6&encoding=json");
  }
}

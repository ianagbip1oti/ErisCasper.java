package com.github.princesslana.eriscasper.rx.websocket;

import com.github.princesslana.eriscasper.immutable.Tuple;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import org.immutables.value.Value;

@Tuple
public interface RxWebSocketEvent {

  WebSocket getWebSocket();

  @Value.Immutable
  interface Closed extends RxWebSocketEvent {
    int getCode();

    String getReason();
  }

  @Value.Immutable
  interface Closing extends RxWebSocketEvent {
    int getCode();

    String getReason();
  }

  @Value.Immutable
  interface Failure extends RxWebSocketEvent {
    Throwable getThrowable();

    Response getRespons();
  }

  @Value.Immutable
  interface ByteStringMessage extends RxWebSocketEvent {
    ByteString getBytes();
  }

  @Value.Immutable
  interface StringMessage extends RxWebSocketEvent {
    String getText();
  }

  @Value.Immutable
  interface Open extends RxWebSocketEvent {
    Response getResponse();
  }
}

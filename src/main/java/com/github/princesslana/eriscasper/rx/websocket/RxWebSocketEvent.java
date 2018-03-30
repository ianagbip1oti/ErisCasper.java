package com.github.princesslana.eriscasper.rx.websocket;

import com.github.princesslana.eriscasper.immutable.Tuple;
import java.util.Optional;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import org.immutables.value.Value;

@Tuple
public interface RxWebSocketEvent {

  @Value.Immutable
  interface Closed extends RxWebSocketEvent {
    WebSocket getWebSocket();

    int getCode();

    String getReason();
  }

  @Value.Immutable
  interface Closing extends RxWebSocketEvent {
    WebSocket getWebSocket();

    int getCode();

    String getReason();
  }

  @Value.Immutable
  interface Failure extends RxWebSocketEvent {
    WebSocket getWebSocket();

    Throwable getThrowable();

    Optional<Response> getResponse();
  }

  @Value.Immutable
  interface ByteStringMessage extends RxWebSocketEvent {
    WebSocket getWebSocket();

    ByteString getBytes();
  }

  @Value.Immutable
  interface StringMessage extends RxWebSocketEvent {
    WebSocket getWebSocket();

    String getText();
  }

  @Value.Immutable
  interface Open extends RxWebSocketEvent {
    WebSocket getWebSocket();

    Response getResponse();
  }
}

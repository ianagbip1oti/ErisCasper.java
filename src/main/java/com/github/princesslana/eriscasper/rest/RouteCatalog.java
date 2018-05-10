package com.github.princesslana.eriscasper.rest;

import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.data.Snowflake;

public final class RouteCatalog {

  private RouteCatalog() {}

  public static Route<Void, GatewayResponse> getGateway() {
    return Route.get("/gateway", GatewayResponse.class);
  }

  public static Route<SendMessageRequest, Message> createMessage(Snowflake channelId) {
    return Route.post(
        String.format("/channels/%s/messages", channelId.unwrap()),
        SendMessageRequest.class,
        Message.class);
  }
}

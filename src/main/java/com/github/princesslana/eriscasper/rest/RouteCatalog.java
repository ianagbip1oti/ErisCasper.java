package com.github.princesslana.eriscasper.rest;

import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.resource.Message;
import com.github.princesslana.eriscasper.rest.channel.CreateMessageRequest;

public final class RouteCatalog {

  private RouteCatalog() {}

  public static Route<Void, GatewayResponse> getGateway() {
    return Route.get("/gateway", GatewayResponse.class);
  }

  public static Route<CreateMessageRequest, Message> createMessage(Snowflake channelId) {
    return Route.post(
        String.format("/channels/%s/messages", channelId.unwrap()),
        CreateMessageRequest.class,
        Message.class);
  }
}

package com.github.princesslana.eriscasper.action;

import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.resource.Message;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.channel.CreateMessageRequest;

public class Actions {
  private Actions() {}

  public static Action<CreateMessageRequest, Message> sendMessage(Snowflake chan, String msg) {
    return ActionTuple.of(RouteCatalog.createMessage(chan), CreateMessageRequest.ofText(msg));
  }
}

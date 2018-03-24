package com.github.princesslana.eriscasper.action;

import com.github.princesslana.eriscasper.data.ChannelId;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.SendMessageRequest;

public class Actions {
  private Actions() {}

  public static Action<SendMessageRequest, Message> sendMessage(ChannelId chan, String msg) {
    return ActionTuple.of(RouteCatalog.createMessage(chan), SendMessageRequest.ofText(msg));
  }
}

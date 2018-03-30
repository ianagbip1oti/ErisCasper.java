package com.github.princesslana.eriscasper.rest;

import com.github.princesslana.eriscasper.data.ChannelId;
import com.github.princesslana.eriscasper.data.Message;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class TestRouteCatalog {

  @Test
  public void createMessage_shouldIncludeChannelId() {
    ChannelId channelId = ChannelId.of("0123456789");

    Route<SendMessageRequest, Message> subject = RouteCatalog.createMessage(channelId);

    Assertions.assertThat(subject.getPath()).isEqualTo("/channels/0123456789/messages");
  }
}

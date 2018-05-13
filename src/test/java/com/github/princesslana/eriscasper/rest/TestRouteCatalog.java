package com.github.princesslana.eriscasper.rest;

import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.resource.Message;
import com.github.princesslana.eriscasper.rest.channel.CreateMessageRequest;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class TestRouteCatalog {

  @Test
  public void createMessage_shouldIncludeChannelId() {
    Snowflake channelId = Snowflake.of("0123456789");

    Route<CreateMessageRequest, Message> subject = RouteCatalog.createMessage(channelId);

    Assertions.assertThat(subject.getPath()).isEqualTo("/channels/0123456789/messages");
  }
}

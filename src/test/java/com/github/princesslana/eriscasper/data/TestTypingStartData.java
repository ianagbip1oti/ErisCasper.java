package com.github.princesslana.eriscasper.data;

import java.time.Instant;
import org.testng.annotations.Test;

public class TestTypingStartData {

  @Test
  public void deserialize_whenValidPayload_shouldDeserialize() {
    String payload =
        "{\"user_id\":\"215210079148834816\",\"timestamp\":1521450931,"
            + "\"channel_id\":\"424363501012779009\"}";

    DataAssert.thatFromJson(payload, TypingStartData.class)
        .hasFieldOrPropertyWithValue("userId", UserId.of("215210079148834816"))
        .hasFieldOrPropertyWithValue("channelId", ChannelId.of("424363501012779009"))
        .hasFieldOrPropertyWithValue("timestamp", Instant.ofEpochSecond(1521450931));
  }
}

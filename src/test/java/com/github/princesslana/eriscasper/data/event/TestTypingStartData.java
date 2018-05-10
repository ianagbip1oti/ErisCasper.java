package com.github.princesslana.eriscasper.data.event;

import com.github.princesslana.eriscasper.data.DataAssert;
import com.github.princesslana.eriscasper.data.Snowflake;
import org.testng.annotations.Test;

public class TestTypingStartData {

  @Test
  public void deserialize_whenValidPayload_shouldDeserialize() {
    String payload =
        "{\"user_id\":\"215210079148834816\",\"timestamp\":1521450931,"
            + "\"channel_id\":\"424363501012779009\"}";

    DataAssert.thatFromJson(payload, TypingStartEventData.class)
        .hasFieldOrPropertyWithValue("userId", Snowflake.of("215210079148834816"))
        .hasFieldOrPropertyWithValue("channelId", Snowflake.of("424363501012779009"))
        .hasFieldOrPropertyWithValue("timestamp", 1521450931L);
  }
}

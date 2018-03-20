package com.github.princesslana.eriscasper.data;

import com.github.princesslana.eriscasper.util.Jackson;
import java.io.IOException;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class TestTypingStartData {

  @Test
  public void deserialize_whenValidPayload_shouldDeserialize() throws IOException {
    String payload =
        "{\"user_id\":\"215210079148834816\",\"timestamp\":1521450931,\"channel_id\":\"424363501012779009\"}";

    TypingStartData d = Jackson.newObjectMapper().readValue(payload, TypingStartData.class);

    Assertions.assertThat(d.getUserId()).isEqualTo(UserId.of("215210079148834816"));
    Assertions.assertThat(d.getChannelId()).isEqualTo(ChannelId.of("424363501012779009"));
    Assertions.assertThat(d.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1521450931));
  }
}

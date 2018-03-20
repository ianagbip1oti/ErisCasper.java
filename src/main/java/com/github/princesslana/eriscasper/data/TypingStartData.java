package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#typing-start">
 *     https://discordapp.com/developers/docs/topics/gateway#typing-start</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableTypingStartData.class)
public interface TypingStartData {
  @JsonProperty("channel_id")
  ChannelId getChannelId();

  @JsonProperty("user_id")
  UserId getUserId();

  Instant getTimestamp();
}

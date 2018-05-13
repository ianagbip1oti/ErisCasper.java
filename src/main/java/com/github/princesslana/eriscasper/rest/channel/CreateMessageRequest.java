package com.github.princesslana.eriscasper.rest.channel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.resource.Embed;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#create-message-params">
 *     https://discordapp.com/developers/docs/resources/channel#create-message-params</a>
 */
@Value.Immutable
public interface CreateMessageRequest {
  String getContent();

  Optional<Snowflake> getNonce();

  @JsonProperty("tts")
  Optional<Boolean> isTts();

  Optional<Embed> getEmbed();

  static CreateMessageRequest ofText(String message) {
    return ImmutableCreateMessageRequest.builder().content(message).build();
  }
}

package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#message-object">
 *     https://discordapp.com/developers/docs/resources/channel#message-object</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableMessage.class)
public interface Message {

  MessageId getId();

  User getAuthor();

  @JsonProperty("channel_id")
  ChannelId getChannelId();

  String getContent();
}

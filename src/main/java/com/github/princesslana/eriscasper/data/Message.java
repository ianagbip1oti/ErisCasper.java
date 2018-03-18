package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableMessage.class)
public interface Message {
  @JsonProperty("channel_id")
  String getChannelId();

  String getContent();
}

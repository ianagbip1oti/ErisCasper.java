package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/resources/user#user-object">
 *     https://discordapp.com/developers/docs/resources/user#user-object</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableUser.class)
public interface User {
  UserId getId();

  String getUsername();

  String getDiscriminator();

  @JsonProperty("bot")
  default boolean isBot() {
    return false;
  }
}

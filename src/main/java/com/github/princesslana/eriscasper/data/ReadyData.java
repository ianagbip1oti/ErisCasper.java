package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.princesslana.eriscasper.immutable.Wrapped;
import com.github.princesslana.eriscasper.immutable.Wrapper;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#ready">
 *     https://discordapp.com/developers/docs/topics/gateway#ready</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableReadyData.class)
public interface ReadyData {

  /** Gateway protocol version */
  Integer getV();

  /** Information about the user including email */
  User getUser();

  /** The guilds the user is in */
  ImmutableList<UnavailableGuild> getGuilds();

  /** Used for resuming connections */
  @JsonProperty("session_id")
  SessionId getSessionId();

  /** Used for debugging - the guilds the user is in */
  @JsonProperty("_trace")
  ImmutableList<String> getTrace();

  @Value.Immutable
  @Wrapped
  interface SessionIdWrapper extends Wrapper<String> {}
}

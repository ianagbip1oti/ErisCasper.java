package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/resources/guild#unavailable-guild-object">
 *     https://discordapp.com/developers/docs/resources/guild#unavailable-guild-object</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableUnavailableGuild.class)
public interface UnavailableGuild {

  /** Guild id */
  GuildId getId();

  /** Is this guild unavailable */
  default boolean isUnavailable() {
    return true;
  }
}

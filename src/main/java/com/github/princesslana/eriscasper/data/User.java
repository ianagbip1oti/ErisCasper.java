package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/resources/user#user-object">
 *     https://discordapp.com/developers/docs/resources/user#user-object</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableUser.class)
public interface User {
  /** The user's id */
  UserId getId();

  /** The user's username, not unique across the platform */
  String getUsername();

  /** The user's 4-digit discord-tag */
  String getDiscriminator();

  /** The user's avatar hash */
  Optional<String> getAvatar();

  /** whether the user belongs to an OAuth2 application */
  @JsonProperty("bot")
  default boolean isBot() {
    return false;
  }

  /** Whether the user has two factor enabled on their account */
  @JsonProperty("mfa_enabled")
  default boolean isMfaEnabled() {
    return false;
  }

  /** whether the email on this account has been verified */
  @JsonProperty("verified")
  default boolean isVerified() {
    return false;
  }

  /** The user's email */
  Optional<String> getEmail();
}

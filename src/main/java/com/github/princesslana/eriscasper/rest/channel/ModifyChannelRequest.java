package com.github.princesslana.eriscasper.rest.channel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.resource.Overwrite;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * @see <a
 *     href="https://discordapp.com/developers/docs/resources/channel#modify-channel-json-params">
 *     https://discordapp.com/developers/docs/resources/channel#modify-channel-json-params</a>
 */
@Value.Immutable
public interface ModifyChannelRequest {
  String getName();

  Integer getPosition();

  Optional<String> getTopic();

  @JsonProperty("nsfw")
  Optional<Boolean> isNsfw();

  Optional<Integer> getBitrate();

  @JsonProperty("user_limit")
  Optional<Integer> getUserLimit();

  @JsonProperty("permission_overwrites")
  ImmutableList<Overwrite> getPermissionOverwrites();

  @JsonProperty("parent_id")
  Snowflake getParentId();
}

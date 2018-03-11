package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#payloads">
 *     https://discordapp.com/developers/docs/topics/gateway#payloads</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutablePayload.class)
public interface Payload {
  Integer op();

  Optional<JsonNode> d();

  Optional<Integer> s();

  Optional<String> t();
}

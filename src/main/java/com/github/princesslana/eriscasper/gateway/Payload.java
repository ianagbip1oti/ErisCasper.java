package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

  Optional<ObjectNode> d();

  Optional<Integer> s();

  Optional<String> t();
}

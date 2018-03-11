package com.github.princesslana.eriscasper.gateway.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#hello">
 *     https://discordapp.com/developers/docs/topics/gateway#hello</a>
 */
@Value.Immutable
public interface Hello {
  Long getHeartbeatInterval();

  @JsonProperty("_trace")
  Collection<String> getTrace();
}

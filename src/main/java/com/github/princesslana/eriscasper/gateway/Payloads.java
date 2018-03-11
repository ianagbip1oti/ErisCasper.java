package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collection;
import org.immutables.value.Value;

public class Payloads {
  private Payloads() {}

  @Value.Immutable
  @JsonDeserialize(as = ImmutableHeartbeat.class)
  public static interface Heartbeat {
    @JsonProperty("heartbeat_interval")
    Long getHeartbeatInterval();

    @JsonProperty("_trace")
    Collection<String> getTrace();
  }
}

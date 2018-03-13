package com.github.princesslana.eriscasper.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

public class Events {

  private Events() {}

  @Value.Immutable
  @JsonDeserialize(as = ImmutableReady.class)
  public static interface Ready extends Event {}
}

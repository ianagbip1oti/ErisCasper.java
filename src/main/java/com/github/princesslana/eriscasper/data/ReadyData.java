package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.princesslana.eriscasper.immutable.Wrapped;
import com.github.princesslana.eriscasper.immutable.Wrapper;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableReadyData.class)
public interface ReadyData {

  User getUser();

  @JsonProperty("session_id")
  SessionId getSessionId();

  @Value.Immutable
  @Wrapped
  interface SessionIdWrapper extends Wrapper<String> {}
}

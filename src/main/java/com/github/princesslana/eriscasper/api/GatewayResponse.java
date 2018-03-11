package com.github.princesslana.eriscasper.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableGatewayResponse.class)
public interface GatewayResponse {
  String getUrl();
}

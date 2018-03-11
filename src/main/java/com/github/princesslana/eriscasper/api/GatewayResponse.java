package com.github.princesslana.eriscasper.api;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(as=ImmutableGatewayResponse.class)
public interface GatewayResponse {
  String getUrl();
}

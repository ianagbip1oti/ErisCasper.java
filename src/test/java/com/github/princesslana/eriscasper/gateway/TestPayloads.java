package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.gateway.Payloads.Heartbeat;
import com.github.princesslana.eriscasper.util.Jackson;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class TestPayloads {

  private final ObjectMapper jackson = Jackson.newObjectMapper();

  @Test
  public void heartbeat_whenValidPayload_shouldDeserialize() throws IOException {
    String payload = "{\"heartbeat_interval\":41250,\"_trace\":[\"gateway-prd-main-wv81\"]}}";

    Heartbeat hb = jackson.readValue(payload, Heartbeat.class);

    Assertions.assertThat(hb.getHeartbeatInterval()).isEqualTo(41250);
    Assertions.assertThat(hb.getTrace()).containsOnly("gateway-prd-main-wv81");
  }
}

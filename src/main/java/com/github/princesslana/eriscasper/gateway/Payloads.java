package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collection;
import org.immutables.value.Value;

public class Payloads {

  private ObjectMapper jackson;

  public Payloads(ObjectMapper jackson) {
    this.jackson = jackson;
  }

  public Payload identify(String token) {
    return identify(ImmutableIdentify.builder().token(token).build());
  }

  public Payload identify(Identify id) {
    return ImmutablePayload.builder().op(OpCode.IDENTIFY).d(jackson.valueToTree(id)).build();
  }

  @Value.Immutable
  @JsonDeserialize(as = ImmutableHeartbeat.class)
  public static interface Heartbeat {
    @JsonProperty("heartbeat_interval")
    Long getHeartbeatInterval();

    @JsonProperty("_trace")
    Collection<String> getTrace();
  }

  /**
   * @see <a href="https://discordapp.com/developers/docs/topics/gateway#identify">
   *     https://discordapp.com/developers/docs/topics/gateway#identify</a>
   */
  // TODO: This structure is not complete
  @Value.Immutable
  public static interface Identify {
    String getToken();

    default ConnectionProperties getProperties() {
      return ConnectionProperties.ofDefault();
    }
  }

  /**
   * @see <a
   *     href="https://discordapp.com/developers/docs/topics/gateway#identify-identify-connection-properties">
   *     https://discordapp.com/developers/docs/topics/gateway#identify-identify-connection-properties</a>
   */
  @Value.Immutable
  public static interface ConnectionProperties {
    @JsonProperty("$os")
    String getOs();

    @JsonProperty("$browser")
    String getBrowser();

    @JsonProperty("$device")
    String getDevice();

    public static ConnectionProperties ofDefault() {
      return ImmutableConnectionProperties.builder()
          .os(System.getProperty("os.name"))
          .browser("ErisCasper.java")
          .device("ErisCasper.java")
          .build();
    }
  }
}

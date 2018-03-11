package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * @see <a href="https://discordapp.com/developers/docs/topics/opcodes-and-status-codes#gateway">
 *     https://discordapp.com/developers/docs/topics/opcodes-and-status-codes#gateway</a>
 */
public enum OpCode {
  DISPATCH(0),
  HEARTBEAT(1),
  IDENTIFY(2),
  STATUS_UPDATE(3),
  VOICE_STATE_UPDATE(4),
  VOICE_SERVER_PING(5),
  RESUME(6),
  RECONNECT(7),
  REQUEST_GUILD_MEMBERS(8),
  INVALID_SESSION(9),
  HELLO(10),
  HEARTBEAT_ACK(11);

  private final int code;

  private OpCode(int code) {
    this.code = code;
  }

  @JsonValue
  public int getCode() {
    return code;
  }

  @JsonCreator
  public static OpCode fromCode(int code) {
    return Arrays.stream(OpCode.values())
        .filter(o -> o.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unknown opcode: " + code));
  }
}

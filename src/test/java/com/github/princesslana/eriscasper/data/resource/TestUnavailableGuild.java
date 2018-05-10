package com.github.princesslana.eriscasper.data.resource;

import com.github.princesslana.eriscasper.data.DataAssert;
import com.github.princesslana.eriscasper.data.Snowflake;
import java.util.Optional;
import org.testng.annotations.Test;

public class TestUnavailableGuild {

  /**
   * @see <a
   *     href="https://discordapp.com/developers/docs/resources/guild#unavailable-guild-object-example-unavailable-guild">
   *     https://discordapp.com/developers/docs/resources/guild#unavailable-guild-object-example-unavailable-guild</a>
   */
  @Test
  public void deserialize_whenExamplePayload_shouldDeseralize() {
    String payload = "{ \"id\": \"41771983423143937\", \"unavailable\": true }";

    DataAssert.thatFromJson(payload, UnavailableGuild.class)
        .hasFieldOrPropertyWithValue("id", Snowflake.of("41771983423143937"))
        .hasFieldOrPropertyWithValue("unavailable", Optional.of(true));
  }
}

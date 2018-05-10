package com.github.princesslana.eriscasper.data.resource;

import com.github.princesslana.eriscasper.data.DataAssert;
import com.github.princesslana.eriscasper.data.Snowflake;
import java.util.Optional;
import org.testng.annotations.Test;

public class TestUserData {

  @Test
  public void deserialize_whenValidPayload_shouldDeserialize() {
    String payload =
        "{     \"username\"     :\"LaBotuel\","
            + "\"id\"           :\"417388135027048495\","
            + "\"discriminator\":\"7013\","
            + "\"bot\"          :true,"
            + "\"avatar\"       :null"
            + "}";

    DataAssert.thatFromJson(payload, User.class)
        .hasFieldOrPropertyWithValue("id", Snowflake.of("417388135027048495"))
        .hasFieldOrPropertyWithValue("username", "LaBotuel")
        .hasFieldOrPropertyWithValue("discriminator", "7013")
        .hasFieldOrPropertyWithValue("avatar", Optional.empty())
        .hasFieldOrPropertyWithValue("bot", Optional.of(true))
        .hasFieldOrPropertyWithValue("mfaEnabled", Optional.empty())
        .hasFieldOrPropertyWithValue("verified", Optional.empty())
        .hasFieldOrPropertyWithValue("email", Optional.empty());
  }

  /**
   * @see <a href="https://discordapp.com/developers/docs/resources/user#user-object-example-user">
   *     https://discordapp.com/developers/docs/resources/user#user-object-example-user</a>
   */
  @Test
  public void deserialize_whenExamplePayload_shouldDeseralize() {
    String payload =
        "{     \"id\"           : \"80351110224678912\","
            + "\"username\"     : \"Nelly\","
            + "\"discriminator\": \"1337\","
            + "\"avatar\"       : \"8342729096ea3675442027381ff50dfe\","
            + "\"verified\"     : true,"
            + "\"email\"        : \"nelly@discordapp.com\""
            + "}";

    DataAssert.thatFromJson(payload, User.class)
        .hasFieldOrPropertyWithValue("id", Snowflake.of("80351110224678912"))
        .hasFieldOrPropertyWithValue("username", "Nelly")
        .hasFieldOrPropertyWithValue("discriminator", "1337")
        .hasFieldOrPropertyWithValue("avatar", Optional.of("8342729096ea3675442027381ff50dfe"))
        .hasFieldOrPropertyWithValue("bot", Optional.empty())
        .hasFieldOrPropertyWithValue("mfaEnabled", Optional.empty())
        .hasFieldOrPropertyWithValue("verified", Optional.of(true))
        .hasFieldOrPropertyWithValue("email", Optional.of("nelly@discordapp.com"));
  }
}

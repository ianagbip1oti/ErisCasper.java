package com.github.princesslana.eriscasper.data;

import com.github.princesslana.eriscasper.util.Jackson;
import java.io.IOException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class TestUserData {

  @Test
  public void deserialize_whenValidPayload_shouldDeserialize() throws IOException {
    String payload =
        "{\"username\":\"LaBotuel\",\"id\":\"417388135027048495\","
            + "\"discriminator\":\"7013\",\"bot\":true,\"avatar\":null}";

    User u = Jackson.newObjectMapper().readValue(payload, User.class);

    Assertions.assertThat(u)
        .hasFieldOrPropertyWithValue("id", UserId.of("417388135027048495"))
        .hasFieldOrPropertyWithValue("username", "LaBotuel")
        .hasFieldOrPropertyWithValue("discriminator", "7013")
        .hasFieldOrPropertyWithValue("avatar", Optional.empty())
        .hasFieldOrPropertyWithValue("bot", true)
        .hasFieldOrPropertyWithValue("mfaEnabled", false)
        .hasFieldOrPropertyWithValue("verified", false)
        .hasFieldOrPropertyWithValue("email", Optional.empty());
  }
}

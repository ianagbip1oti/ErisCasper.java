package com.github.princesslana.eriscasper.faker;

import com.github.princesslana.eriscasper.BotToken;
import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.gateway.SessionId;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class DiscordFaker {
  private DiscordFaker() {}

  /**
   * Generates a fake snowflake id.
   *
   * <p>For now this is just a random Long, because we don't make use of the snowflake id structure.
   */
  public static Snowflake snowflake() {
    return Snowflake.of(Long.toString(RandomUtils.nextLong()));
  }

  public static BotToken botToken() {
    return BotToken.of(RandomStringUtils.randomAlphanumeric(32));
  }

  public static SessionId sessionId() {
    return SessionId.of(RandomStringUtils.random(32));
  }
}

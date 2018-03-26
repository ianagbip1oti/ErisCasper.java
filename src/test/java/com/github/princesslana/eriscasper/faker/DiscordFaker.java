package com.github.princesslana.eriscasper.faker;

import com.github.princesslana.eriscasper.data.SessionId;
import com.github.princesslana.eriscasper.data.Snowflake;
import java.util.function.Function;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class DiscordFaker {
  private DiscordFaker() {}

  /**
   * Generates a fake snowflake id.
   *
   * <p>For now this is just a random Long, because we don't make use of the snowflake id structure.
   */
  public static <T extends Snowflake> T snowflake(Function<String, T> f) {
    return f.apply(Long.toString(RandomUtils.nextLong()));
  }

  public static SessionId sessionId() {
    return SessionId.of(RandomStringUtils.random(32));
  }
}

package com.github.princesslana.eriscasper.faker;

import com.github.javafaker.Faker;
import com.github.princesslana.eriscasper.data.ImmutableUser;
import com.github.princesslana.eriscasper.data.User;
import com.github.princesslana.eriscasper.data.UserId;
import org.apache.commons.lang3.RandomStringUtils;

public class UserFaker {

  private UserFaker() {}

  public static User user() {
    return ImmutableUser.builder()
        .id(userId())
        .username(username())
        .discriminator(discriminator())
        .build();
  }

  public static UserId userId() {
    return DiscordFaker.snowflake(UserId::of);
  }

  public static String username() {
    return Faker.instance().name().name();
  }

  public static String discriminator() {
    return RandomStringUtils.randomNumeric(4);
  }
}

package com.github.princesslana.eriscasper.faker;

import com.github.javafaker.Faker;
import com.github.princesslana.eriscasper.data.ChannelId;
import com.github.princesslana.eriscasper.data.ImmutableMessage;
import com.github.princesslana.eriscasper.data.ImmutableReadyData;
import com.github.princesslana.eriscasper.data.ImmutableUser;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.data.MessageId;
import com.github.princesslana.eriscasper.data.ReadyData;
import com.github.princesslana.eriscasper.data.User;
import com.github.princesslana.eriscasper.data.UserId;
import org.apache.commons.lang3.RandomStringUtils;

public class DataFaker {
  private DataFaker() {}

  public static ChannelId channelId() {
    return DiscordFaker.snowflake(ChannelId::of);
  }

  public static String discriminator() {
    return RandomStringUtils.randomNumeric(4);
  }

  public static Message message() {
    return ImmutableMessage.builder()
        .id(messageId())
        .author(user())
        .channelId(channelId())
        .content(Faker.instance().chuckNorris().fact())
        .build();
  }

  public static MessageId messageId() {
    return DiscordFaker.snowflake(MessageId::of);
  }

  public static ReadyData ready() {
    return ImmutableReadyData.builder()
        .v(6)
        .user(user())
        .sessionId(DiscordFaker.sessionId())
        .build();
  }

  public static User user() {
    return ImmutableUser.builder()
        .id(userId())
        .username(username())
        .discriminator(discriminator())
        .build();
  }

  public static String username() {
    return Faker.instance().name().name();
  }

  public static UserId userId() {
    return DiscordFaker.snowflake(UserId::of);
  }
}

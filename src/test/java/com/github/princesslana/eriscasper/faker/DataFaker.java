package com.github.princesslana.eriscasper.faker;

import com.github.princesslana.eriscasper.data.ImmutableReadyData;
import com.github.princesslana.eriscasper.data.ReadyData;

public class DataFaker {
  private DataFaker() {}

  public static ReadyData ready() {
    return ImmutableReadyData.builder()
        .v(6)
        .user(UserFaker.user())
        .sessionId(DiscordFaker.sessionId())
        .build();
  }
}

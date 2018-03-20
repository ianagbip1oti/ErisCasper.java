package com.github.princesslana.eriscasper.event;

import com.github.princesslana.eriscasper.data.GuildCreateData;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.data.ReadyData;
import com.github.princesslana.eriscasper.data.ResumedData;
import com.github.princesslana.eriscasper.data.TypingStartData;
import org.immutables.value.Value;

public class Events {

  private Events() {}

  @Value.Immutable
  public static interface GuildCreate extends Event<GuildCreateData> {}

  @Value.Immutable
  public static interface MessageCreate extends Event<Message> {}

  @Value.Immutable
  public static interface Ready extends Event<ReadyData> {}

  @Value.Immutable
  public static interface Resumed extends Event<ResumedData> {}

  @Value.Immutable
  public static interface TypingStart extends Event<TypingStartData> {}
}

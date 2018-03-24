package com.github.princesslana.eriscasper.event;

import com.github.princesslana.eriscasper.data.GuildCreateData;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.data.ReadyData;
import com.github.princesslana.eriscasper.data.ResumedData;
import com.github.princesslana.eriscasper.data.TypingStartData;
import com.github.princesslana.eriscasper.immutable.Tuple;
import org.immutables.value.Value;

@Tuple
public class Events {

  private Events() {}

  @Value.Immutable
  public static interface GuildCreate extends Event {
    GuildCreateData getData();
  }

  @Value.Immutable
  public static interface MessageCreate extends Event {
    Message getData();
  }

  @Value.Immutable
  public static interface Ready extends Event {
    ReadyData getData();
  }

  @Value.Immutable
  public static interface Resumed extends Event {
    ResumedData getData();
  }

  @Value.Immutable
  public static interface TypingStart extends Event {
    TypingStartData getData();
  }
}

package com.github.princesslana.eriscasper.event;

import com.github.princesslana.eriscasper.data.GuildCreateData;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.data.ReadyData;
import com.github.princesslana.eriscasper.data.ResumedData;
import com.github.princesslana.eriscasper.data.TypingStartData;
import java.util.function.Function;

/**
 * Possible event types that we can receive from Discord.
 *
 * <p>Each EventType is associated with an {@link EventFactory} so that it is known how to construct
 * an {@link Event} from a {@link com.github.princesslana.eriscasper.gateway.Payload}.
 *
 * <p>It is tempting to try and merge EventType and {@link EventFactory} into a single class, but we
 * can not match the dataClass and factory types using generics within the EventType enum.
 *
 * @see <a
 *     href="https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events">
 *     https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events</a>
 */
public enum EventType {
  GUILD_CREATE(GuildCreateData.class, GuildCreate::of),
  MESSAGE_CREATE(Message.class, MessageCreate::of),
  READY(ReadyData.class, Ready::of),
  RESUMED(ResumedData.class, Resumed::of),
  TYPING_START(TypingStartData.class, TypingStart::of);

  private final EventFactory<?> factory;

  private EventType(EventFactory<?> factory) {
    this.factory = factory;
  }

  private <T> EventType(Class<T> dataClass, Function<T, Event> factory) {
    this(EventFactoryTuple.of(dataClass, factory));
  }

  public Class<?> getDataClass() {
    return factory.getDataClass();
  }

  public Event newEvent(Object data) {
    return factory.apply(data);
  }
}

package com.github.princesslana.eriscasper.event;

import com.github.princesslana.eriscasper.immutable.Tuple;
import java.util.function.Function;
import org.immutables.value.Value;

@Value.Immutable
@Tuple
public abstract class EventFactory<D> {

  public abstract Class<D> getDataClass();

  protected abstract Function<D, Event> getConstructor();

  /**
   * Takes a data payload and constructs a given event from it.
   *
   * <p>It would be nice to have the types be more specific, but they are deliberately left generic
   * due to difficulties in making the types work where this method is used ({@link
   * com.github.princesslana.eriscasper.gateway.Payloads#toEvent(Event)})
   *
   * @param data data for the event to construct
   * @return an event with the given data as the payload
   * @throw ClassCastException if the data is not an instance of that given in {@link
   *     #getDataClass()}
   */
  public Event apply(Object data) {
    return getConstructor().apply(getDataClass().cast(data));
  }
}

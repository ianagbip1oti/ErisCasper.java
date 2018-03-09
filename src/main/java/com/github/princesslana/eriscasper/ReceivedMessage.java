package com.github.princesslana.eriscasper;

import io.reactivex.Completable;
import io.reactivex.Observable;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.immutables.value.Value;

@Value.Immutable
public interface ReceivedMessage {

  User getAuthor();

  Message getMessage();

  TextChannel getSource();

  public static Completable sendReply(ReceivedMessage to, String with) {
    return sendReply(to, Message.from(with));
  }

  public static Completable sendReply(ReceivedMessage to, Message with) {
    return Message.send(with, to.getSource());
  }

  public static ReceivedMessage from(MessageReceivedEvent evt) {
    return ImmutableReceivedMessage.builder()
        .author(evt.getAuthor())
        .message(Message.from(evt.getMessage()))
        .source(evt.getTextChannel())
        .build();
  }

  public static Observable<ReceivedMessage> from(ErisCasper ec) {
    return from(ec.events());
  }

  public static Observable<ReceivedMessage> from(Observable<Event> evts) {
    return evts.ofType(MessageReceivedEvent.class).map(ReceivedMessage::from);
  }
}

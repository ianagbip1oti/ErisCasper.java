package com.github.princesslana.eriscasper;

import io.reactivex.Completable;
import io.reactivex.Observable;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {

  private static final Logger LOG = LoggerFactory.getLogger(Message.class);

  private final net.dv8tion.jda.core.entities.Message jdaMessage;

  private Message(net.dv8tion.jda.core.entities.Message jdaMessage) {
    this.jdaMessage = jdaMessage;
  }

  public String getContent() {
    return jdaMessage.getContentRaw();
  }

  // TODO: This doesn't follow the contract of Completable,
  //   since it does not wait for a subscription to execute
  public Completable reply(String content) {
    return Completable.fromFuture(
        jdaMessage
            .getTextChannel()
            .sendMessage(new MessageBuilder().append(content).build())
            .submit());
  }

  public static Message create(MessageReceivedEvent evt) {
    return new Message(evt.getMessage());
  }

  public static Observable<Message> from(ErisCasper ec) {
    return from(ec.events());
  }

  public static Observable<Message> from(Observable<Event> evts) {
    return evts.ofType(MessageReceivedEvent.class)
        .map(Message::create)
        .doOnNext(m -> LOG.debug("Message received: {}", m.getContent()));
  }
}

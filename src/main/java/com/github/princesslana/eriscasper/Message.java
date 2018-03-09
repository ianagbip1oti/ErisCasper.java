package com.github.princesslana.eriscasper;

import io.reactivex.Completable;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.immutables.value.Value;

@Value.Immutable
public interface Message {

  String getContent();

  public static Completable send(Message m, TextChannel to) {
    return Completable.fromAction(
        () -> to.sendMessage(new MessageBuilder().append(m.getContent()).build()).complete());
  }

  public static Message from(String content) {
    return ImmutableMessage.builder().content(content).build();
  }

  public static Message from(net.dv8tion.jda.core.entities.Message jdaMsg) {
    return from(jdaMsg.getContentRaw());
  }
}

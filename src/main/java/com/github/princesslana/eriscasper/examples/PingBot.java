package com.github.princesslana.eriscasper.examples;

import com.github.princesslana.eriscasper.Bot;
import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.action.Actions;
import com.github.princesslana.eriscasper.data.event.MessageCreateEvent;
import io.reactivex.Completable;

public class PingBot implements Bot {

  @Override
  public Completable apply(BotContext ctx) {
    return ctx.on(
        MessageCreateEvent.class,
        recv -> {
          if (recv.getContent().equals("+ping")) {
            return ctx.execute(Actions.sendMessage(recv.getChannelId(), "pong"));
          }

          return ctx.doNothing();
        });
  }

  public static void main(String[] args) {
    ErisCasper.create().run(new PingBot());
  }
}

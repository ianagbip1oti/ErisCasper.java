package com.github.princesslana.eriscasper.examples;

import com.github.princesslana.eriscasper.Bot;
import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.action.Actions;
import com.github.princesslana.eriscasper.event.MessageCreate;
import com.github.princesslana.eriscasper.repository.RepositoryDefinition;
import io.reactivex.Completable;

public class WhoAreYouBot implements Bot {

  @Override
  public Completable apply(BotContext ctx) {
    return ctx.on(
        MessageCreate.class,
        recv -> {
          if (recv.getContent().equals("+whoareyou")) {
            return ctx.getRepository(RepositoryDefinition.USER)
                .getSelf()
                .map(
                    s ->
                        Actions.sendMessage(
                            recv.getChannelId(),
                            "I'm " + s.getUsername() + ". Who the hell are you?"))
                .flatMapCompletable(ctx::execute);
          }

          return ctx.doNothing();
        });
  }

  public static void main(String[] args) {
    ErisCasper.create().run(new WhoAreYouBot());
  }
}

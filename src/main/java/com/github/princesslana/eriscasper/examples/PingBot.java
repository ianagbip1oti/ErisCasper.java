package com.github.princesslana.eriscasper.examples;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.event.Events;
import com.github.princesslana.eriscasper.rest.ImmutableSendMessageRequest;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import io.reactivex.Completable;

public class PingBot {

  public static void main(String[] args) {
    ErisCasper.create()
        .run(
            ctx -> {
              Completable ping =
                  ctx.getEvents()
                      .ofType(Events.MessageCreate.class)
                      .map(Event::getData)
                      .filter(d -> d.getContent().equals("+ping"))
                      .map(d -> RouteCatalog.createMessage(d.getChannelId()))
                      .flatMapCompletable(
                          r ->
                              ctx.execute(
                                      r,
                                      ImmutableSendMessageRequest.builder().content("pong").build())
                                      .toCompletable());

              Completable testPing =
                  ctx.getEvents()
                      .ofType(Events.MessageCreate.class)
                      .map(Event::getData)
                      .filter(b -> b.getContent().equals("+test"))
                      .map(b -> RouteCatalog.createMessage(b.getChannelId()))
                      .flatMapCompletable(
                          x ->
                              ctx.execute(
                                      x,
                                      ImmutableSendMessageRequest.builder().content("testPhrase").build())
                                      .toCompletable());

              return Completable.mergeArray(ping, testPing);
            });
  }
}

package com.github.princesslana.eriscasper.examples;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.event.Events;
import com.github.princesslana.eriscasper.rest.ImmutableSendMessageRequest;
import com.github.princesslana.eriscasper.rest.RouteCatalog;

public class PingBot {

  public static void main(String[] args) {
    ErisCasper.create()
        .run(
            ctx ->
                ctx.getEvents()

                    // only message create events
                    .ofType(Events.MessageCreate.class)
                    .map(Event::getData)

                    // only with content "+ping"
                    .filter(d -> d.getContent().equals("+ping"))

                    // send create message request
                    .map(d -> RouteCatalog.createMessage(d.getChannelId()))
                    .flatMapCompletable(
                        r ->
                            ctx.execute(
                                    r,
                                    ImmutableSendMessageRequest.builder().content("pong").build())
                                .toCompletable()));
  }
}

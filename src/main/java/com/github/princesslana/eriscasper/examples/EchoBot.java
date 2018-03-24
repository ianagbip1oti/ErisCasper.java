package com.github.princesslana.eriscasper.examples;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.event.Events;
import com.github.princesslana.eriscasper.rest.ImmutableSendMessageRequest;
import com.github.princesslana.eriscasper.rest.RouteCatalog;

public class EchoBot {
  public static void main(String args[]) {
    ErisCasper.create()
        .run(
            ctx ->
                ctx.getEvents()

                    // Same type as PingBot in examples
                    .ofType(Events.MessageCreate.class)
                    .map(Events.MessageCreate::getData)

                    // Need to check for bot's own message
                    .filter(d -> !d.getAuthor().isBot())
                    .filter(d -> d.getContent().startsWith("+echo"))
                    .flatMapCompletable(
                        d -> {
                          String replyMessage = d.getContent().replaceFirst("\\+echo", "");

                          // Empty Arguments
                          if (replyMessage.trim().isEmpty()) {
                            replyMessage = "This command requires 1 argument";
                          }

                          return ctx.execute(
                                  RouteCatalog.createMessage(d.getChannelId()),
                                  ImmutableSendMessageRequest.builder()
                                      .content(replyMessage)
                                      .build())
                              .toCompletable();
                        }));
  }
}

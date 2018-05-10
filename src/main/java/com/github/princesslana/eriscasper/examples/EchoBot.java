package com.github.princesslana.eriscasper.examples;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.event.MessageCreate;
import com.github.princesslana.eriscasper.rest.ImmutableSendMessageRequest;
import com.github.princesslana.eriscasper.rest.RouteCatalog;

public class EchoBot {
  public static void main(String args[]) {
    ErisCasper.create()
        .run(
            ctx ->
                ctx.getEvents()

                    // Same type as PingBot in examples
                    .ofType(MessageCreate.class)
                    .map(MessageCreate::unwrap)

                    // Need to check for bot's own message
                    .filter(d -> !d.getAuthor().isBot().orElse(false))
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

package com.github.princesslana.eriscasper.examples;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.event.Events;
import com.github.princesslana.eriscasper.rest.ImmutableSendMessageRequest;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import io.reactivex.Completable;

public class EchoBot {
    public static void main(String args[]) {
        ErisCasper.create()
            .run(
                ctx ->
                    ctx.getEvents()
                            .ofType(Events.MessageCreate.class)
                            .map(Event::getData)
                            .filter(d -> d.getContent().startsWith("+echo"))

                            .flatMapCompletable( d -> {
                                String replyMessage = d.getContent().replaceFirst("\\+echo", "");
                                if (replyMessage.startsWith("")) {
                                    replyMessage = replyMessage;
                                } else {
                                    System.out.println("Error Message");
                                }
                                return ctx.execute(RouteCatalog.createMessage(d.getChannelId()),
                                        ImmutableSendMessageRequest.builder().content(replyMessage).build()).toCompletable();
                            }));
    }
}


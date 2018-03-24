package com.github.princesslana.eriscasper.api;

import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.data.Users;
import com.github.princesslana.eriscasper.rest.ImmutableSendMessageRequest;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.SendMessageRequest;
import io.reactivex.Completable;
import java.util.regex.Matcher;

public class RobotContext {

  private final BotContext bctx;
  private final Matcher matcher;
  private final Message message;

  public RobotContext(BotContext bctx, Matcher matcher, Message message) {
    this.bctx = bctx;
    this.matcher = matcher;
    this.message = message;
  }

  public boolean matches() {
    return matcher.matches();
  }

  public String match(int group) {
    return matcher.group(group);
  }

  public String match(String name) {
    return matcher.group(name);
  }

  public Completable reply(String msg) {
    return send(Users.mentionByNickname(message.getAuthor()) + " " + msg);
  }

  public Completable send(String msg) {
    SendMessageRequest req = ImmutableSendMessageRequest.builder().content(msg).build();

    return bctx.execute(RouteCatalog.createMessage(message.getChannelId()), req).toCompletable();
  }
}

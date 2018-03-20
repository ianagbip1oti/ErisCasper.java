package com.github.princesslana.eriscasper.api;

import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.data.Users;
import com.github.princesslana.eriscasper.rest.ImmutableSendMessageRequest;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.SendMessageRequest;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class RobotContext {

  private final Matcher matcher;
  private final Message message;

  private final List<Function<BotContext, Completable>> actions = new ArrayList<>();

  public RobotContext(Matcher matcher, Message message) {
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

  public Completable actions(BotContext bctx) {
    return Observable.fromIterable(actions).flatMapCompletable(a -> a.apply(bctx));
  }

  public void reply(String msg) {
    send(Users.mentionByNickname(message.getAuthor()) + " " + msg);
  }

  public void send(String msg) {
    SendMessageRequest req = ImmutableSendMessageRequest.builder().content(msg).build();

    actions.add(
        bctx ->
            bctx.execute(RouteCatalog.createMessage(message.getChannelId()), req).toCompletable());
  }
}

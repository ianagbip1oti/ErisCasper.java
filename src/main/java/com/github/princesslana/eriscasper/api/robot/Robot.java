package com.github.princesslana.eriscasper.api.robot;

import com.github.princesslana.eriscasper.Bot;
import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.Bots;
import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.data.Users;
import com.github.princesslana.eriscasper.data.event.MessageCreateEvent;
import com.github.princesslana.eriscasper.data.resource.Message;
import com.github.princesslana.eriscasper.data.resource.User;
import com.github.princesslana.eriscasper.repository.RepositoryDefinition;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Robot presents an API that allows execution of callbacks when messages matching regexes are
 * received.
 */
public class Robot implements Bot {

  private List<Bot> bots = new ArrayList<>();

  @Override
  public Completable apply(BotContext bctx) {
    return Bots.merge(bots).apply(bctx);
  }

  /**
   * See {@link hear(Pattern, Function)}.
   *
   * @param regex regex to match
   * @param f function to execute when matched
   */
  public void hear(String regex, Function<RobotContext, Completable> f) {
    hear(Pattern.compile(regex), f);
  }

  /**
   * Checks all messages sent against the provided regex. Executes the provided function when there
   * is a match.
   *
   * @param regex regex to match
   * @param f function to execute when matched
   */
  public void hear(Pattern regex, Function<RobotContext, Completable> f) {
    respond(regex, f, "");
  }

  /**
   * See {@link respond(Pattern, Function)}.
   *
   * @param regex regex to match
   * @param f function to execute when matched
   */
  public void respond(String regex, Function<RobotContext, Completable> f) {
    respond(Pattern.compile(regex), f);
  }

  /**
   * Checks all messages sent directly to the Robot against the provided regex. Executes the
   * provided function when there is a match.
   *
   * <p>Messages are considered to be directed at the Robot if they begin with "+", the username of
   * the bot account, or a mention of the bot account.
   *
   * @param regex regex to match
   * @param f function to execute when matched
   */
  public void respond(Pattern regex, Function<RobotContext, Completable> f) {
    respond(regex, f, "+");
    respond(regex, f, bctx -> getSelf(bctx).map(s -> s.getUsername() + " "));
    respond(regex, f, bctx -> getSelf(bctx).map(s -> Users.mention(s) + " "));
  }

  private void respond(Pattern regex, Function<RobotContext, Completable> f, String prefix) {
    respond(regex, f, __ -> Single.just(prefix));
  }

  private void respond(
      Pattern regex,
      Function<RobotContext, Completable> f,
      Function<BotContext, Single<String>> prefix) {

    BiFunction<BotContext, Message, Maybe<Message>> startsWithPrefix =
        (btx, m) ->
            prefix
                .apply(btx)
                .flatMapMaybe(p -> m.getContent().startsWith(p) ? Maybe.just(m) : Maybe.empty());

    BiFunction<BotContext, Message, Single<RobotContext>> toRobotContext =
        (bctx, m) ->
            prefix
                .apply(bctx)
                .map(p -> StringUtils.removeStart(m.getContent(), p))
                .map(c -> new RobotContext(bctx, regex.matcher(c), m));

    bots.add(
        bctx ->
            messages(bctx)
                .flatMapMaybe(m -> startsWithPrefix.apply(bctx, m))
                .flatMapSingle(msg -> toRobotContext.apply(bctx, msg))
                .filter(RobotContext::matches)
                .flatMapCompletable(f));
  }

  /** Run this Robot on a newly created ErisCasper instance. */
  public void run() {
    run(ErisCasper.create());
  }

  /**
   * Run this Robot on the provided ErisCasper instance.
   *
   * @param ec an ErisCasper instance to run on
   */
  public void run(ErisCasper ec) {
    ec.run(this);
  }

  private static Observable<Message> messages(BotContext bctx) {
    return bctx.getEvents()
        .ofType(MessageCreateEvent.class)
        .map(MessageCreateEvent::unwrap)
        .filter(m -> !m.getAuthor().isBot().orElse(false));
  }

  private static Single<User> getSelf(BotContext bctx) {
    return bctx.getRepository(RepositoryDefinition.USER).getSelf();
  }
}

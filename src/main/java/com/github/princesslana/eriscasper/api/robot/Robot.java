package com.github.princesslana.eriscasper.api.robot;

import com.github.princesslana.eriscasper.Bot;
import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.Bots;
import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.event.MessageCreate;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class Robot implements Bot {

  private List<Bot> bots = new ArrayList<>();

  public Completable apply(BotContext bctx) {
    return Bots.merge(bots).apply(bctx);
  }

  public void hear(String regex, Function<RobotContext, Completable> f) {
    hear(Pattern.compile(regex), f);
  }

  public void hear(Pattern regex, Function<RobotContext, Completable> f) {
    listen(regex, f, Optional.empty());
  }

  public void listen(String regex, Function<RobotContext, Completable> f) {
    listen(Pattern.compile(regex), f);
  }

  public void listen(Pattern regex, Function<RobotContext, Completable> f) {
    // TODO: Add more prefixes. e.g., name, which requires BotContext#getSelf
    listen(regex, f, Optional.of("+"));
  }

  private void listen(
      Pattern regex, Function<RobotContext, Completable> f, Optional<String> prefix) {
    Predicate<Message> startsWithPrefix =
        m -> prefix.map(p -> m.getContent().startsWith(p)).orElse(true);

    BiFunction<BotContext, Message, RobotContext> toRobotContext =
        (bctx, m) -> {
          String content =
              prefix.map(p -> StringUtils.removeStart(m.getContent(), p)).orElse(m.getContent());

          return new RobotContext(bctx, regex.matcher(content), m);
        };

    bots.add(
        bctx ->
            messages(bctx)
                .filter(startsWithPrefix)
                .map(msg -> toRobotContext.apply(bctx, msg))
                .filter(RobotContext::matches)
                .flatMapCompletable(f));
  }

  public void run() {
    run(ErisCasper.create());
  }

  public void run(ErisCasper ec) {
    ec.run(this);
  }

  private static Observable<Message> messages(BotContext bctx) {
    return bctx.getEvents()
        .ofType(MessageCreate.class)
        .map(MessageCreate::unwrap)
        .filter(m -> !m.getAuthor().isBot());
  }
}

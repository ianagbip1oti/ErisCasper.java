package com.github.princesslana.eriscasper.api;

import com.github.princesslana.eriscasper.Bot;
import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.Bots;
import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.data.Message;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.event.Events;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class Robot {

  private List<Bot> bots = new ArrayList<>();

  public void hear(String regex, Consumer<RobotContext> f) {
    hear(Pattern.compile(regex), f);
  }

  public void hear(Pattern regex, Consumer<RobotContext> f) {
    listen(regex, f, Optional.empty());
  }

  public void listen(String regex, Consumer<RobotContext> f) {
    listen(Pattern.compile(regex), f);
  }

  public void listen(Pattern regex, Consumer<RobotContext> f) {
    // TODO: Add more prefixes. e.g., name, which requires BotContext#getSelf
    listen(regex, f, Optional.of("+"));
  }

  private void listen(Pattern regex, Consumer<RobotContext> f, Optional<String> prefix) {
    Predicate<Message> startsWithPrefix =
        m -> prefix.map(p -> m.getContent().startsWith(p)).orElse(true);

    Function<Message, RobotContext> toRobotContext =
        m -> {
          String content =
              prefix.map(p -> StringUtils.removeStart(m.getContent(), p)).orElse(m.getContent());

          return new RobotContext(regex.matcher(content), m);
        };

    bots.add(
        bctx ->
            messages(bctx)
                .filter(startsWithPrefix)
                .map(toRobotContext)
                .filter(RobotContext::matches)
                .doOnNext(f::accept)
                .flatMapCompletable(rctx -> rctx.actions(bctx)));
  }

  public void run() {
    run(ErisCasper.create());
  }

  public void run(ErisCasper ec) {
    ec.run(Bots.merge(bots));
  }

  private static Flowable<Message> messages(BotContext bctx) {
    return bctx.getEvents()
        .ofType(Events.MessageCreate.class)
        .map(Event::getData)
        .filter(m -> !m.getAuthor().isBot());
  }
}

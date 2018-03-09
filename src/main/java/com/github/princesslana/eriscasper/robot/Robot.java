package com.github.princesslana.eriscasper.robot;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.ReceivedMessage;
import com.github.princesslana.eriscasper.immutable.Tuple;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.regex.Pattern;
import net.dv8tion.jda.core.entities.SelfUser;
import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

public class Robot {

  private static final String CHAR_PREFIX = "+";

  private final ErisCasper ec;

  public Robot(ErisCasper ec) {
    this.ec = ec;
  }

  public void hear(String regex, Consumer<Context> run) {
    hear(Pattern.compile(regex), run);
  }

  public void hear(Pattern regex, Consumer<Context> run) {
    ReceivedMessage.from(ec)
        .map(m -> new Context(regex.matcher(m.getMessage().getContent()), m))
        .filter(Context::isMatch)
        .subscribe(run);
  }

  public void respond(String regex, Consumer<Context> run) {
    respond(Pattern.compile(regex), run);
  }

  public void respond(Pattern regex, Consumer<Context> run) {
    Observable<SelfUser> self = ec.self().toObservable();
    Observable<ReceivedMessage> msgs = ReceivedMessage.from(ec);

    Observable.combineLatest(self, msgs, (s, m) -> SelfAndMessageTuple.of(s, m))
        .flatMapMaybe(sam -> toListenContext(regex, sam))
        .filter(Context::isMatch)
        .subscribe(run);
  }

  private Maybe<Context> toListenContext(Pattern regex, SelfAndMessage sam) {
    Predicate<String> startsWithPrefix =
        pfx -> StringUtils.startsWithIgnoreCase(sam.getContent(), pfx);
    Function<String, String> stripPrefix = pfx -> sam.getContent().substring(pfx.length()).trim();

    return Observable.fromArray(CHAR_PREFIX, sam.getSelf().getName(), sam.getSelf().getAsMention())
        .filter(startsWithPrefix)
        .firstElement()
        .map(stripPrefix)
        .map(m -> new Context(regex.matcher(m), sam.getReceived()));
  }

  public static Robot from(ErisCasper ec) {
    return new Robot(ec);
  }

  @Value.Immutable
  @Tuple
  public static interface SelfAndMessage {

    SelfUser getSelf();

    ReceivedMessage getReceived();

    default String getContent() {
      return getReceived().getMessage().getContent();
    }
  }
}

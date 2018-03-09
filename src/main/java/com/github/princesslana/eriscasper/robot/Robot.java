package com.github.princesslana.eriscasper.robot;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.ReceivedMessage;
import io.reactivex.functions.Consumer;
import java.util.regex.Pattern;

public class Robot {

  private final ErisCasper ec;

  public Robot(ErisCasper ec) {
    this.ec = ec;
  }

  public void hear(String regex, Consumer<Context> run) {
    hear(Pattern.compile(regex), run);
  }

  public void hear(Pattern regex, Consumer<Context> run) {
    ReceivedMessage.from(ec)
        .map(m -> new Context(regex, m))
        .filter(Context::isMatch)
        .subscribe(run);
  }

  public void listen(Pattern regex, Consumer<Context> run) {
    ReceivedMessage.from(ec)
        .map(m -> new Context(regex, m))
        .filter(Context::isMatch)
        .subscribe(run);
  }

  // private Maybe<Message> startsWithName(String s) {
  //  ec.self().map(User::getName)
  //
  // }

  public static Robot from(ErisCasper ec) {
    return new Robot(ec);
  }
}

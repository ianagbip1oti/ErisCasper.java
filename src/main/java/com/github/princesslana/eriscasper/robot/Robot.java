package com.github.princesslana.eriscasper.robot;

import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.Message;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import java.util.regex.Pattern;
import net.dv8tion.jda.core.events.Event;

public class Robot {

  private final Observable<Event> events;

  public Robot(Observable<Event> events) {
    this.events = events;
  }

  public void hear(String regex, Consumer<Context> run) {
    hear(Pattern.compile(regex), run);
  }

  public void hear(Pattern regex, Consumer<Context> run) {
    Message.from(events).map(m -> new Context(regex, m)).filter(Context::isMatch).subscribe(run);
  }

  public static Robot from(ErisCasper ec) {
    return new Robot(ec.events());
  }
}

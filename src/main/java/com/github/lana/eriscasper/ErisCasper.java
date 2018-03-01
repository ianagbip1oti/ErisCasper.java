package com.github.lana.eriscasper;

import io.reactivex.Observable;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

public class ErisCasper {

  private final Observable<Event> events;

  public ErisCasper(Observable<Event> events) {
    this.events = events;
  }

  public Observable<Event> events() {
    return events;
  }

  public static ErisCasper create() {
    try {
      return create(
          new JDABuilder(AccountType.BOT).setToken(System.getenv("EC_TOKEN")).buildBlocking());
    } catch (InterruptedException | LoginException e) {
      throw new ErisCasperFatalException("Error building JDA instance", e);
    }
  }

  public static ErisCasper create(JDA jda) {
    Observable<Event> events =
        Observable.create(
            emitter -> {
              EventListener el = emitter::onNext;
              jda.addEventListener(el);
            });

    return new ErisCasper(events);
  }
}

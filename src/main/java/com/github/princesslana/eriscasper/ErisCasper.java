package com.github.princesslana.eriscasper;

import io.reactivex.Observable;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

public class ErisCasper {

  private final JDA jda;

  private final Observable<Event> events;

  public ErisCasper(JDA jda) {
    this.jda = jda;

    this.events =
        Observable.create(
            emitter -> {
              EventListener el = emitter::onNext;
              jda.addEventListener(el);
            });
  }

  public User getSelf() {
    return jda.getSelfUser();
  }

  public Observable<Event> events() {
    return events;
  }

  public static ErisCasper create() {
    try {
      return create(new JDABuilder().setToken(System.getenv("EC_TOKEN")).buildBlocking());
    } catch (InterruptedException | LoginException e) {
      throw new ErisCasperFatalException("Error building JDA instance", e);
    }
  }

  public static ErisCasper create(JDA jda) {
    return new ErisCasper(jda);
  }
}

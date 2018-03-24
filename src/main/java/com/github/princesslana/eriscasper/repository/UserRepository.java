package com.github.princesslana.eriscasper.repository;

import com.github.princesslana.eriscasper.data.User;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.event.Events;
import io.reactivex.Observable;
import io.reactivex.Single;

public class UserRepository {

  private User self;

  public UserRepository() {}

  public Single<User> getSelf() {
    return Single.just(self);
  }

  public void connect(Observable<Event> events) {
    events
        .ofType(Events.Ready.class)
        .map(Events.Ready::getData)
        .map(d -> d.getUser())
        .subscribe(u -> self = u);
  }
}

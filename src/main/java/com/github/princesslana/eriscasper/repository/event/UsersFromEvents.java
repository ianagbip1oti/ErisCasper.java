package com.github.princesslana.eriscasper.repository.event;

import com.github.princesslana.eriscasper.data.event.Event;
import com.github.princesslana.eriscasper.data.event.ReadyEvent;
import com.github.princesslana.eriscasper.data.resource.User;
import com.github.princesslana.eriscasper.repository.UserRepository;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observables.ConnectableObservable;

public class UsersFromEvents implements UserRepository {

  private ConnectableObservable<User> self;

  public UsersFromEvents(Observable<Event> events) {
    self = events.ofType(ReadyEvent.class).map(ReadyEvent::unwrap).map(d -> d.getUser()).replay(1);

    self.connect();
  }

  public Single<User> getSelf() {
    return self.firstOrError();
  }
}

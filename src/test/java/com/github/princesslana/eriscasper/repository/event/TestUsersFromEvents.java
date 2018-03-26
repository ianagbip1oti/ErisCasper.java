package com.github.princesslana.eriscasper.repository.event;

import com.github.princesslana.eriscasper.data.ImmutableReadyData;
import com.github.princesslana.eriscasper.data.ReadyData;
import com.github.princesslana.eriscasper.data.User;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.event.Ready;
import com.github.princesslana.eriscasper.faker.DataFaker;
import com.github.princesslana.eriscasper.faker.UserFaker;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestUsersFromEvents {

  private PublishSubject<Event> events = PublishSubject.create();

  private UsersFromEvents subject;

  @BeforeMethod
  public void subject() {
    subject = new UsersFromEvents(events);
  }

  @Test
  public void getSelf_whenAfterReady_shouldReturnSelf() {
    TestObserver<User> observer = new TestObserver<>();

    User self = UserFaker.user();
    ReadyData ready = ImmutableReadyData.copyOf(DataFaker.ready()).withUser(self);
    events.onNext(Ready.of(ready));

    subject.getSelf().subscribe(observer);

    observer.assertComplete();
    observer.assertValues(self);
  }
}

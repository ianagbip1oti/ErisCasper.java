package com.github.princesslana.eriscasper.repository.event;

import com.github.princesslana.eriscasper.data.event.Event;
import com.github.princesslana.eriscasper.data.event.ImmutableReadyEventData;
import com.github.princesslana.eriscasper.data.event.ReadyEvent;
import com.github.princesslana.eriscasper.data.event.ReadyEventData;
import com.github.princesslana.eriscasper.data.resource.User;
import com.github.princesslana.eriscasper.faker.DataFaker;
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

    User self = DataFaker.user();
    ReadyEventData ready = ImmutableReadyEventData.copyOf(DataFaker.ready()).withUser(self);
    events.onNext(ReadyEvent.of(ready));

    subject.getSelf().subscribe(observer);

    observer.assertComplete();
    observer.assertValues(self);
  }
}

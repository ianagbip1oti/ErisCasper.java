package com.github.lana.eriscasper;

import io.reactivex.observers.TestObserver;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class TestErisCasper {

  @Test
  public void shouldEmitEvents() {
    JDA mockJda = Mockito.mock(JDA.class);
    Event mockEvent = Mockito.mock(Event.class);

    ArgumentCaptor<EventListener> el = ArgumentCaptor.forClass(EventListener.class);
    TestObserver<Event> observer = new TestObserver<>();

    ErisCasper ec = ErisCasper.create(mockJda);
    ec.events().subscribe(observer);

    Mockito.verify(mockJda).addEventListener(el.capture());

    el.getValue().onEvent(mockEvent);
    observer.assertValuesOnly(mockEvent);
  }
}

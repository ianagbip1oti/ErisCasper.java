package com.github.princesslana.eriscasper.examples.robot;

import static org.mockito.BDDMockito.then;

import com.github.princesslana.eriscasper.Bot;
import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.data.ImmutableMessage;
import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.Users;
import com.github.princesslana.eriscasper.data.event.Event;
import com.github.princesslana.eriscasper.data.resource.User;
import com.github.princesslana.eriscasper.event.MessageCreate;
import com.github.princesslana.eriscasper.faker.DataFaker;
import com.github.princesslana.eriscasper.faker.DiscordFaker;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.Routes;
import com.github.princesslana.eriscasper.rest.SendMessageRequest;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPingAndEchoRobot {

  @Mock private Routes routes;

  private Bot subject;

  @BeforeMethod
  public void subject() {
    MockitoAnnotations.initMocks(this);
    subject = PingAndEchoRobot.create();
  }

  @Test
  public void apply_whenPing_shouldPong() {
    PublishSubject<Event> events = PublishSubject.create();

    BotContext ctx = new BotContext(events, routes, null);

    TestObserver<Void> subscriber = subject.apply(ctx).test();

    Snowflake channelId = DiscordFaker.snowflake();
    User author = DataFaker.user();

    events.onNext(
        MessageCreate.of(
            ImmutableMessage.copyOf(DataFaker.message())
                .withAuthor(author)
                .withContent("+ping")
                .withChannelId(channelId)));

    String expectedResponse = Users.mentionByNickname(author) + " pong";

    then(routes)
        .should()
        .execute(
            RouteCatalog.createMessage(channelId), SendMessageRequest.ofText(expectedResponse));

    subscriber.assertNotComplete();
  }
}

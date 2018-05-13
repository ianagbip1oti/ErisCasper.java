package com.github.princesslana.eriscasper.api.robot;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.notNull;
import static org.mockito.BDDMockito.then;

import com.github.javafaker.Faker;
import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.Users;
import com.github.princesslana.eriscasper.data.event.Event;
import com.github.princesslana.eriscasper.data.event.MessageCreateEvent;
import com.github.princesslana.eriscasper.data.resource.ImmutableMessage;
import com.github.princesslana.eriscasper.data.resource.ImmutableUser;
import com.github.princesslana.eriscasper.data.resource.User;
import com.github.princesslana.eriscasper.faker.DataFaker;
import com.github.princesslana.eriscasper.faker.DiscordFaker;
import com.github.princesslana.eriscasper.repository.RepositoryDefinition;
import com.github.princesslana.eriscasper.repository.RepositoryManager;
import com.github.princesslana.eriscasper.repository.UserRepository;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.Routes;
import com.github.princesslana.eriscasper.rest.channel.CreateMessageRequest;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestRobot {

  @Mock private Routes routes;

  @Mock private RepositoryManager repositoryManager;
  @Mock private UserRepository userRepository;

  private Robot subject;

  private BotContext bctx;
  private final PublishSubject<Event> events = PublishSubject.create();

  @BeforeMethod
  public void subject() {
    MockitoAnnotations.initMocks(this);
    subject = new Robot();

    bctx = new BotContext(events, routes, repositoryManager);

    given(repositoryManager.get(RepositoryDefinition.USER)).willReturn(userRepository);
    given(userRepository.getSelf()).willReturn(Single.just(DataFaker.user()));

    // This mocks out the message create endpoint.
    // Returning a fake message does not match with the actual endpoint
    // which will return a Message based on the SendMessageRequest.
    // This works for us here because we never check the result.
    given(routes.execute(notNull(), notNull())).willReturn(Single.just(DataFaker.message()));
  }

  @Test
  public void hear_whenPing_shouldSendPong() {
    subject.hear("ping", ctx -> ctx.send("pong"));
    TestObserver<Void> subscriber = run();

    Snowflake channelId = DiscordFaker.snowflake();

    events.onNext(
        MessageCreateEvent.of(
            ImmutableMessage.copyOf(DataFaker.message())
                .withContent("ping")
                .withChannelId(channelId)));

    thenShouldSend(channelId, "pong");
    subscriber.assertNotTerminated();
  }

  @Test
  public void respond_whenForPing_shouldReplyPong() {
    subject.respond("ping", ctx -> ctx.reply("pong"));
    TestObserver<Void> subscriber = run();

    Snowflake channelId = DiscordFaker.snowflake();
    User author = DataFaker.user();

    events.onNext(
        MessageCreateEvent.of(
            ImmutableMessage.copyOf(DataFaker.message())
                .withAuthor(author)
                .withContent("+ping")
                .withChannelId(channelId)));

    String expectedResponse = Users.mentionByNickname(author) + " pong";

    thenShouldSend(channelId, expectedResponse);
    subscriber.assertNotTerminated();
  }

  @Test
  public void respond_whenForPing_shouldNotHear() {
    subject.respond("ping", ctx -> ctx.reply("pong"));
    TestObserver<Void> subscriber = run();

    events.onNext(
        MessageCreateEvent.of(ImmutableMessage.copyOf(DataFaker.message()).withContent("ping")));

    then(routes).shouldHaveZeroInteractions();
    subscriber.assertNotTerminated();
  }

  @Test
  public void respond_whenForPing_shouldIgnoreBot() {
    subject.respond("ping", ctx -> ctx.reply("pong"));
    TestObserver<Void> subscriber = run();

    User author = ImmutableUser.copyOf(DataFaker.user()).withIsBot(true);

    events.onNext(
        MessageCreateEvent.of(
            ImmutableMessage.copyOf(DataFaker.message()).withAuthor(author).withContent("+ping")));

    then(routes).shouldHaveZeroInteractions();
    subscriber.assertNotTerminated();
  }

  @Test
  public void respond_whenForEchoRegex_shouldSendEcho() {
    subject.respond("echo (.+)", ctx -> ctx.send(ctx.match(1)));
    TestObserver<Void> subscriber = run();

    Snowflake channelId = DiscordFaker.snowflake();

    String fact = Faker.instance().chuckNorris().fact();

    events.onNext(
        MessageCreateEvent.of(
            ImmutableMessage.copyOf(DataFaker.message())
                .withContent("+echo " + fact)
                .withChannelId(channelId)));

    thenShouldSend(channelId, fact);
    subscriber.assertNotTerminated();
  }

  private TestObserver<Void> run() {
    return subject.apply(bctx).test();
  }

  private void thenShouldSend(Snowflake channelId, String msg) {
    then(routes)
        .should()
        .execute(RouteCatalog.createMessage(channelId), CreateMessageRequest.ofText(msg));
  }
}

package com.github.princesslana.eriscasper.gateway;

import com.github.princesslana.eriscasper.BotToken;
import com.github.princesslana.eriscasper.data.SessionId;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.event.Events;
import com.github.princesslana.eriscasper.rx.Singles;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocket;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocketEvent;
import com.google.common.base.Preconditions;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.operator.RateLimiterOperator;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gateway {

  private static final Logger LOG = LoggerFactory.getLogger(Gateway.class);

  /**
   * @see <a href="https://discordapp.com/developers/docs/topics/gateway#sending-payloads">
   *     https://discordapp.com/developers/docs/topics/gateway#sending-payloads</a>
   */
  private static final int MAX_MESSAGE_SIZE = 4096;

  private static final String VERSION = "6";
  private static final String ENCODING = "json";

  private final OkHttpClient client;
  private final Payloads payloads;

  private Optional<SequenceNumber> lastSeenSequenceNumber = Optional.empty();

  private Optional<SessionId> sessionId = Optional.empty();

  /**
   * @see <a href="https://discordapp.com/developers/docs/topics/gateway#rate-limiting">
   *     https://discordapp.com/developers/docs/topics/gateway#rate-limiting</a>
   */
  private final RateLimiter sendLimit =
      RateLimiter.of(
          "Gateway#sendLimit",
          RateLimiterConfig.custom()
              .limitRefreshPeriod(Duration.ofSeconds(60))
              .limitForPeriod(120)
              .timeoutDuration(Duration.ofSeconds(60))
              .build());

  /**
   * @see <a href="https://discordapp.com/developers/docs/topics/gateway#identifying">
   *     https://discordapp.com/developers/docs/topics/gateway#identifying</a>
   */
  private final RateLimiter identifyLimit =
      RateLimiter.of(
          "Gateway#identifyLimit",
          RateLimiterConfig.custom()
              .limitRefreshPeriod(Duration.ofSeconds(5))
              .limitForPeriod(1)
              .timeoutDuration(Duration.ofSeconds(60))
              .build());

  public Gateway(OkHttpClient client, Payloads payloads) {
    this.client = client;
    this.payloads = payloads;
  }

  private boolean isResumable() {
    return sessionId.isPresent() && lastSeenSequenceNumber.isPresent();
  }

  private void setSessionId(SessionId sid) {
    this.sessionId = Optional.of(sid);
  }

  private void sequenceNumberSeen(Optional<SequenceNumber> seq) {
    seq.ifPresent(sid -> lastSeenSequenceNumber = Optional.of(sid));
  }

  public Flowable<Event<?>> connect(String url, BotToken token) {
    RxWebSocket ws = new RxWebSocket(client);

    Flowable<Payload> ps =
        ws.connect(String.format("%s?v=%s&encoding=%s", url, VERSION, ENCODING))
            .ofType(RxWebSocketEvent.StringMessage.class)
            .map(RxWebSocketEvent.StringMessage::getText)
            .flatMapMaybe(
                Singles.toMaybeAnd(
                    payloads::read, (s, t) -> LOG.warn("Error reading payload: {}", s, t)))
            .doOnNext(p -> sequenceNumberSeen(p.s()))
            .share();

    Completable heartbeat =
        ps.filter(Payload.isOp(OpCode.HELLO)).flatMapCompletable(p -> heartbeat(ws, p));

    Completable identify =
        ps.filter(Payload.isOp(OpCode.HELLO))
            .flatMapCompletable(p -> isResumable() ? resume(ws, token) : identify(ws, token));

    Flowable<Event<?>> events = ps.flatMapMaybe(payloads::toEvent).share();

    Completable setSessionId =
        events
            .ofType(Events.Ready.class)
            .map(r -> r.getData().getSessionId())
            .doOnNext(this::setSessionId)
            .ignoreElements();

    return Flowable.merge(
            Flowable.just(
                events, setSessionId.toFlowable(), heartbeat.toFlowable(), identify.toFlowable()))
        .doOnNext(e -> LOG.debug("Event: {}.", e));
  }

  private Completable send(RxWebSocket ws, Payload payload) {
    return payloads
        .writeToString(payload)
        .lift(RateLimiterOperator.of(sendLimit))
        .filter(s -> s.getBytes().length <= MAX_MESSAGE_SIZE)
        .doOnComplete(() -> LOG.warn("Payload rejected as too long: {}.", payload))
        .flatMapCompletable(ws::send)
        .doOnComplete(() -> LOG.debug("Sent: {}.", payload));
  }

  private Completable identify(RxWebSocket ws, BotToken token) {
    return Single.just(payloads.identify(token))
        .lift(RateLimiterOperator.of(identifyLimit))
        .flatMapCompletable(p -> send(ws, p));
  }

  private Completable heartbeat(RxWebSocket ws, Payload hello) {
    return payloads
        .dataAs(hello, Payloads.Heartbeat.class)
        .flatMapObservable(
            h -> Observable.interval(h.getHeartbeatInterval(), TimeUnit.MILLISECONDS))
        .flatMapCompletable(l -> send(ws, payloads.heartbeat(lastSeenSequenceNumber)));
  }

  public Completable resume(RxWebSocket ws, BotToken token) {
    Preconditions.checkState(sessionId.isPresent(), "Can not resume without a session id");
    Preconditions.checkState(
        lastSeenSequenceNumber.isPresent(), "Can not resume without a sequence number");

    return Single.just(
            ImmutableResume.builder()
                .token(token)
                .sessionId(sessionId.get())
                .seq(lastSeenSequenceNumber.get())
                .build())
        .map(payloads::resume)
        .flatMapCompletable(p -> send(ws, p));
  }
}

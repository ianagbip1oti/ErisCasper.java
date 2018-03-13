package com.github.princesslana.eriscasper.gateway;

import com.github.princesslana.eriscasper.rx.websocket.RxWebSocket;
import com.github.princesslana.eriscasper.rx.websocket.RxWebSocketEvent;
import com.google.common.io.Closer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.operator.RateLimiterOperator;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.flowables.ConnectableFlowable;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gateway implements Closeable {

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

  private final Closer closer = Closer.create();

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

  public Flowable<Payload> connect(String url, String token) {
    RxWebSocket ws = closer.register(new RxWebSocket(client));

    Flowable<Payload> ps =
        ws.connect(String.format("%s?v=%s&encoding=%s", url, VERSION, ENCODING))
            .ofType(RxWebSocketEvent.StringMessage.class)
            .map(RxWebSocketEvent.StringMessage::getText)
            .flatMapSingle(payloads::read)
            .share();

    Completable heartbeat = ps.filter(Payload.isOp(OpCode.HELLO)).flatMapCompletable(p -> heartbeat(ws, p));

    Completable identify = ps.filter(Payload.isOp(OpCode.HELLO)).flatMapCompletable(p -> identify(ws, token));

    return Flowable.merge(Flowable.just(ps, heartbeat.<Payload>toFlowable(), identify.<Payload>toFlowable()));
  }

  private Completable send(RxWebSocket ws, Payload payload) {
    return payloads
        .writeToString(payload)
        .lift(RateLimiterOperator.of(sendLimit))
        .filter(s -> s.getBytes().length <= MAX_MESSAGE_SIZE)
        .doOnComplete(() -> LOG.warn("Payload rejected as too long: {}.", payload))
        .flatMapCompletable(ws::send);
  }

  private Completable identify(RxWebSocket ws, String token) {
    return Single.just(payloads.identify(token))
        .lift(RateLimiterOperator.of(identifyLimit))
        .flatMapCompletable(p -> send(ws, p));
  }

  private Completable heartbeat(RxWebSocket ws, Payload hello) {
    return payloads
        .dataAs(hello, Payloads.Heartbeat.class)
        .flatMapObservable(h -> Observable.interval(h.getHeartbeatInterval(), TimeUnit.MILLISECONDS))
        .flatMapCompletable(l -> send(ws, payloads.heartbeat()));
  }

  @Override
  public void close() throws IOException {
    closer.close();
  }
}

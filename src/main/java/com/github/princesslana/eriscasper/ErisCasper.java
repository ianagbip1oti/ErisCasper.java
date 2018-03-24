package com.github.princesslana.eriscasper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.event.Event;
import com.github.princesslana.eriscasper.gateway.Gateway;
import com.github.princesslana.eriscasper.gateway.Payloads;
import com.github.princesslana.eriscasper.repository.RepositoryManager;
import com.github.princesslana.eriscasper.rest.RouteCatalog;
import com.github.princesslana.eriscasper.rest.Routes;
import com.github.princesslana.eriscasper.util.Jackson;
import com.github.princesslana.eriscasper.util.OkHttp;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErisCasper {

  private static final Logger LOG = LoggerFactory.getLogger(ErisCasper.class);

  private final BotToken token;

  private final OkHttpClient httpClient = OkHttp.newHttpClient();
  private final ObjectMapper jackson = Jackson.newObjectMapper();
  private final Payloads payloads = new Payloads(jackson);

  private final Routes routes;

  private ErisCasper(BotToken token) {
    this.token = token;

    routes = new Routes(token, httpClient, jackson);
  }

  private Observable<Event> getEvents() {
    Gateway gateway = Gateway.create(httpClient, payloads);

    return Single.just(RouteCatalog.getGateway())
        .observeOn(Schedulers.io())
        .flatMap(routes::execute)
        .toObservable()
        .flatMap(gr -> gateway.connect(gr.getUrl(), token))
        .observeOn(Schedulers.computation())
        .share();
  }

  public void run(Bot bot) {
    Observable<Event> events = getEvents();

    RepositoryManager rm = RepositoryManager.create();

    rm.connect(events);

    bot.apply(new BotContext(events, routes, rm))
        .doOnError(t -> LOG.warn("Exception thrown by Bot", t))
        .retry()
        .blockingAwait();
  }

  public static ErisCasper create() {
    return create(System.getenv("EC_TOKEN"));
  }

  public static ErisCasper create(String token) {
    return new ErisCasper(BotToken.of(token));
  }
}

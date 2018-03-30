package com.github.princesslana.eriscasper.rest;

import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.princesslana.eriscasper.BotToken;
import com.github.princesslana.eriscasper.faker.DiscordFaker;
import com.github.princesslana.eriscasper.util.Jackson;
import com.google.common.io.Closer;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestRoutes {

  private final BotToken token = DiscordFaker.botToken();

  @Mock private OkHttpClient mockHttpClient;
  @Mock private Call mockCall;

  private ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);

  private ObjectMapper jackson = Jackson.newObjectMapper();

  private Routes subject;

  private final Closer closer = Closer.create();

  @BeforeMethod
  public void subject() {
    MockitoAnnotations.initMocks(this);

    subject = new Routes(token, mockHttpClient, jackson);

    given(mockHttpClient.newCall(request.capture())).willReturn(mockCall);
  }

  @AfterMethod
  public void close() throws IOException {
    closer.close();
  }

  @Test
  public void execute_whenGet_shouldSendAuthorizationHeader() {
    Route<Void, String> route = Route.get("/test/path", String.class);

    TestObserver<String> subscriber = execute(route);

    Assertions.assertThat(request.getValue().header("Authorization"))
        .isEqualTo("Bot " + token.unwrap());
  }

  @Test
  public void execute_whenGet_shouldCallDiscordUrl() {
    Route<Void, String> route = Route.get("/test/path", String.class);

    TestObserver<String> subscriber = execute(route);

    Assertions.assertThat(request.getValue().url().host()).isEqualTo("discordapp.com");
    Assertions.assertThat(request.getValue().url().isHttps()).isTrue();
    Assertions.assertThat(request.getValue().url().pathSegments())
        .containsExactly("api", "v6", "test", "path");
  }

  @Test
  public void execute_whenGet_shouldEmitResponse() throws IOException {
    Route<Void, String> route = Route.get("/test/path", String.class);

    givenRespondsWith("test_response");

    TestObserver<String> subscriber = execute(route);

    subscriber.assertComplete();
    subscriber.assertValues("test_response");
  }

  private <O> TestObserver<O> execute(Route<Void, O> route) {
    try {
      TestObserver<O> subscriber = subject.execute(route).test();
      subscriber.await(5, TimeUnit.SECONDS);
      return subscriber;
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  private void givenRespondsWith(Object o) throws IOException {
    Buffer b = closer.register(new Buffer().writeUtf8(jackson.writeValueAsString(o)));

    ResponseBody rsBody = new RealResponseBody(null, b.size(), b);

    // we have to use answer here so that request.getValue() is not evaluated immediately
    given(mockCall.execute())
        .willAnswer(
            a ->
                new Response.Builder()
                    .protocol(Protocol.HTTP_1_1)
                    .request(request.getValue())
                    .code(200)
                    .message("OK")
                    .body(rsBody)
                    .build());
  }
}

package com.github.princesslana.eriscasper.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#payloads">
 *     https://discordapp.com/developers/docs/topics/gateway#payloads</a>
 */
@Value.Immutable
@JsonDeserialize(as = ImmutablePayload.class)
public abstract class Payload {
  public abstract OpCode op();

  protected abstract Optional<JsonNode> d();

  protected abstract Optional<Integer> s();

  protected abstract Optional<String> t();

  public <T> Single<T> d(ObjectMapper jackson, Class<T> clazz) {
    return Single.fromCallable(() -> jackson.readerFor(clazz).readValue(d().get()));
  }

  public static Predicate<Payload> isOp(OpCode op) {
    return p -> p.op() == op;
  }
}

package com.github.princesslana.eriscasper.rx;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

public class Singles {

  private Singles() {}

  public static <A, T> Function<A, Maybe<T>> toMaybeAnd(
      Function<A, Single<T>> s, BiConsumer<A, Throwable> onError) {
    return a -> s.apply(a).toMaybe().doOnError(e -> onError.accept(a, e)).onErrorComplete();
  }
}

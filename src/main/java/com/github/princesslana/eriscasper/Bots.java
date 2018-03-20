package com.github.princesslana.eriscasper;

import io.reactivex.Observable;

public class Bots {
  private Bots() {}

  public static Bot merge(Iterable<? extends Bot> bots) {
    return merge(Observable.fromIterable(bots));
  }

  public static Bot merge(Observable<? extends Bot> bots) {
    return ctx -> bots.flatMapCompletable(b -> b.apply(ctx));
  }
}

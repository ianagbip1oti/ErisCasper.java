package com.github.princesslana.eriscasper;

import io.reactivex.Completable;
import io.reactivex.functions.Function;

/**
 * A Bot is a function that converts events into actions.
 *
 * <p>We use the reactivex Function types (rather than Java's) for easier use with rxjava methods.
 */
public interface Bot extends Function<BotContext, Completable> {

  /**
   * Run this Bot.
   *
   * <p>This differs from the overriden {@link Function#apply(Object)} in that it does not throw a
   * checked Exception.
   */
  Completable apply(BotContext ctx);
}

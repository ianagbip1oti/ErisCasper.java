package com.github.princesslana.eriscasper.action;

import com.github.princesslana.eriscasper.immutable.Tuple;
import com.github.princesslana.eriscasper.rest.Route;
import org.immutables.value.Value;

@Value.Immutable
@Tuple
public interface Action<I, O> {
  Route<I, O> getRoute();

  I getData();
}

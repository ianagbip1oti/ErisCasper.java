package com.github.princesslana.eriscasper.immutable;

import org.immutables.value.Value;

public interface Wrapper<T> {
  @Value.Parameter
  T unwrap();
}

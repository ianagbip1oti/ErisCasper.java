package com.github.princesslana.eriscasper.immutable;

import com.fasterxml.jackson.annotation.JsonValue;
import org.immutables.value.Value;

public interface Wrapper<T> {
  @Value.Parameter
  @JsonValue
  public abstract T unwrap();
}

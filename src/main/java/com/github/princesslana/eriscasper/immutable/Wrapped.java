package com.github.princesslana.eriscasper.immutable;

import org.immutables.value.Value;

@Value.Style(
  typeAbstract = "*Wrapper",
  typeImmutable = "*",
  defaults = @Value.Immutable(builder = false, copy = false)
)
public @interface Wrapped {}

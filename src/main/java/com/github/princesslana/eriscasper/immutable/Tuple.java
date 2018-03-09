package com.github.princesslana.eriscasper.immutable;

import org.immutables.value.Value;

@Value.Style(
  allParameters = true,
  typeImmutable = "*Tuple",
  defaults = @Value.Immutable(builder = false)
)
public @interface Tuple {}

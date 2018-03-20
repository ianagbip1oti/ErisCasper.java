package com.github.princesslana.eriscasper.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableResumedData.class)
public interface ResumedData {}

package com.github.princesslana.eriscasper.data;

import com.github.princesslana.eriscasper.util.Jackson;
import java.io.IOException;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

public class DataAssert {
  private DataAssert() {}

  public static <T> AbstractObjectAssert<?, T> thatFromJson(String payload, Class<T> cls) {
    try {
      T data = Jackson.newObjectMapper().readValue(payload, cls);

      return Assertions.assertThat(data);
    } catch (IOException e) {
      throw new AssertionError("Could not parse payload", e);
    }
  }
}

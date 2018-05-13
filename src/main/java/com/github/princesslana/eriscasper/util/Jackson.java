package com.github.princesslana.eriscasper.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Jackson {
  private Jackson() {}

  public static ObjectMapper newObjectMapper() {
    ObjectMapper jackson = new ObjectMapper();
    jackson.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jackson.registerModule(new GuavaModule());
    jackson.registerModule(new Jdk8Module());
    jackson.registerModule(new JavaTimeModule());

    InjectableValues inject = new InjectableValues.Std().addValue(ObjectMapper.class, jackson);
    jackson.setInjectableValues(inject);

    return jackson;
  }
}

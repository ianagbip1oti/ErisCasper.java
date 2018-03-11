package com.github.princesslana.eriscasper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class Jackson {
  private Jackson() {}

  public static ObjectMapper newObjectMapper() {
    ObjectMapper jackson = new ObjectMapper();
    jackson.registerModule(new Jdk8Module());
    return jackson;
  }
}

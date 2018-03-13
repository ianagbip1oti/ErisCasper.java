package com.github.princesslana.eriscasper.event;

public enum EventType {
  READY(Events.Ready.class);

  private Class<? extends Event> dataClass;

  private EventType(Class<? extends Event> dataClass) {
    this.dataClass = dataClass;
  }

  public Class<? extends Event> getDataClass() {
    return dataClass;
  }
}

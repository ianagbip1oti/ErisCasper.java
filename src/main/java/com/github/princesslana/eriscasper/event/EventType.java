package com.github.princesslana.eriscasper.event;

public enum EventType {
  GUILD_CREATE(Events.GuildCreate.class),
  READY(Events.Ready.class);

  private Class<? extends Event> dataClass;

  private EventType(Class<? extends Event> dataClass) {
    this.dataClass = dataClass;
  }

  public Class<? extends Event> getDataClass() {
    return dataClass;
  }
}

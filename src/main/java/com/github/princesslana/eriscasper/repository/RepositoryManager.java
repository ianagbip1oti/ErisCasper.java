package com.github.princesslana.eriscasper.repository;

import com.github.princesslana.eriscasper.event.Event;
import com.google.common.base.Preconditions;
import io.reactivex.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RepositoryManager {
  private final ConcurrentMap<String, Object> repositories = new ConcurrentHashMap<>();

  private RepositoryManager() {}

  @SuppressWarnings("unchecked")
  public <R> R get(RepositoryDefinition<R> def) {
    Preconditions.checkState(
        repositories.containsKey(def.getName()), "No such repository: %s", def.getName());

    return (R) repositories.get(def.getName());
  }

  private <R> void put(RepositoryDefinition<R> def, R repository) {
    Preconditions.checkState(
        !repositories.containsKey(def.getName()),
        "Duplicate repository created: %s",
        def.getName());

    repositories.put(def.getName(), repository);
  }

  public void connect(Observable<Event> events) {
    get(RepositoryDefinition.USER).connect(events);
  }

  public static RepositoryManager create() {
    RepositoryManager rm = new RepositoryManager();
    rm.put(RepositoryDefinition.USER, new UserRepository());
    return rm;
  }
}

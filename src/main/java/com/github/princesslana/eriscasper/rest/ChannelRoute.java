package com.github.princesslana.eriscasper.rest;

import com.github.princesslana.eriscasper.data.Snowflake;
import com.github.princesslana.eriscasper.data.resource.Channel;
import com.github.princesslana.eriscasper.rest.channel.ModifyChannelRequest;

public class ChannelRoute {

  private final Snowflake id;

  private ChannelRoute(Snowflake id) {
    this.id = id;
  }

  public Route<Void, Channel> getChannel() {
    return Route.get(path("/"), Channel.class);
  }

  public Route<ModifyChannelRequest, Channel> modifyChannel() {
    return Route.put(path("/"), ModifyChannelRequest.class, Channel.class);
  }

  public Route<Void, Channel> deleteChannel() {
    return Route.delete(path("/"), Channel.class);
  }

  private String path(String path) {
    return "/channels/" + id.unwrap() + path;
  }

  public static ChannelRoute on(Snowflake id) {
    return new ChannelRoute(id);
  }

  public static ChannelRoute on(Channel channel) {
    return on(channel.getId());
  }
}

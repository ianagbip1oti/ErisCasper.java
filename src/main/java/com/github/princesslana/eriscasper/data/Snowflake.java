package com.github.princesslana.eriscasper.data;

import com.github.princesslana.eriscasper.immutable.Wrapped;
import com.github.princesslana.eriscasper.immutable.Wrapper;
import org.immutables.value.Value;

@Wrapped
public interface Snowflake extends Wrapper<String> {

  @Value.Immutable
  interface ChannelIdWrapper extends Snowflake {}

  @Value.Immutable
  interface GuildIdWrapper extends Snowflake {}

  @Value.Immutable
  interface MessageIdWrapper extends Snowflake {}

  @Value.Immutable
  interface UserIdWrapper extends Snowflake {}
}

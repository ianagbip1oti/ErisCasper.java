package com.github.princesslana.eriscasper;

import com.github.princesslana.eriscasper.data.immutable.Wrapped;
import com.github.princesslana.eriscasper.data.immutable.Wrapper;
import org.immutables.value.Value;

@Value.Immutable
@Wrapped
public interface BotTokenWrapper extends Wrapper<String> {}

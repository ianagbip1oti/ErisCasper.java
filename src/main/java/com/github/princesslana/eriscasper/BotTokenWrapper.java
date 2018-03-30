package com.github.princesslana.eriscasper;

import com.github.princesslana.eriscasper.immutable.Wrapped;
import com.github.princesslana.eriscasper.immutable.Wrapper;
import org.immutables.value.Value;

@Value.Immutable
@Wrapped
public interface BotTokenWrapper extends Wrapper<String> {}

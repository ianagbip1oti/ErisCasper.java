package com.github.princesslana.eriscasper.robot;

import com.github.princesslana.eriscasper.ReceivedMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Context {

  private final ReceivedMessage received;

  private final Matcher matcher;

  public Context(Pattern regex, ReceivedMessage received) {
    this.received = received;
    this.matcher = regex.matcher(received.getMessage().getContent());
  }

  public boolean isMatch() {
    return matcher.matches();
  }

  public String getGroup(int index) {
    return matcher.group(index);
  }

  public String getGroup(String name) {
    return matcher.group(name);
  }

  public void reply(String reply) {
    send(String.format("%s %s", received.getAuthor().getAsMention(), reply));
  }

  public void send(String reply) {
    ReceivedMessage.sendReply(received, reply).subscribe();
  }
}

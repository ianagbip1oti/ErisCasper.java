package com.github.princesslana.eriscasper.robot;

import com.github.princesslana.eriscasper.ReceivedMessage;
import java.util.regex.Matcher;

public class Context {

  private final Matcher matcher;

  private final ReceivedMessage received;

  public Context(Matcher matcher, ReceivedMessage received) {
    this.matcher = matcher;
    this.received = received;
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

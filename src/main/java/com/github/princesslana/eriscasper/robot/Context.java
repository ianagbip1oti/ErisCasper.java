package com.github.princesslana.eriscasper.robot;

import com.github.princesslana.eriscasper.Message;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Context {

  private final Message message;

  private final Matcher matcher;

  public Context(Pattern regex, Message message) {
    this.message = message;
    this.matcher = regex.matcher(message.getContent());
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
    message.reply(reply).subscribe();
  }
}

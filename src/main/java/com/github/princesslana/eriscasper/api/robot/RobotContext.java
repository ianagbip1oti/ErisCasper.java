package com.github.princesslana.eriscasper.api.robot;

import com.github.princesslana.eriscasper.BotContext;
import com.github.princesslana.eriscasper.action.Actions;
import com.github.princesslana.eriscasper.data.Users;
import com.github.princesslana.eriscasper.data.resource.Message;
import io.reactivex.Completable;
import java.util.regex.Matcher;

/** RobotContext is provided to callbacks setup in Robot. */
public class RobotContext {

  private final BotContext bctx;
  private final Matcher matcher;
  private final Message message;

  /**
   * Creates a RobotContext based upon the provided parameters.
   *
   * @param bctx the core BotContext
   * @param matcher matcher used by the regex on the message
   * @param message the message that was received
   */
  public RobotContext(BotContext bctx, Matcher matcher, Message message) {
    this.bctx = bctx;
    this.matcher = matcher;
    this.message = message;
  }

  /**
   * Whether the received message was matched by the matcher.
   *
   * @returns the result of the matcher
   */
  public boolean matches() {
    return matcher.matches();
  }

  /**
   * Gets the value captured by the indexed group as matched in the message.
   *
   * @param group index of the capturing group
   * @return the value captured by the group
   */
  public String match(int group) {
    return matcher.group(group);
  }

  /**
   * Gets the value captured by the named group as matched in the message.
   *
   * @param name name of the capturing group
   * @return the value captured by the group
   */
  public String match(String name) {
    return matcher.group(name);
  }

  /**
   * Sends a reply to the message triggering the callback. The user that sent the message will be
   * pinged.
   *
   * @param msg the message to send
   */
  public Completable reply(String msg) {
    return send(Users.mentionByNickname(message.getAuthor()) + " " + msg);
  }

  /**
   * Sends a message to the room of the message the triggered the callback.
   *
   * @param msg the message to send
   */
  public Completable send(String msg) {
    return bctx.execute(Actions.sendMessage(message.getChannelId(), msg));
  }
}

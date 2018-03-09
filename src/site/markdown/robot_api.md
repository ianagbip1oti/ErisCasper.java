# Robot API

The Robot API is based of that used by [hubot](https://hubot.github.com/).
It allows for a bot to reply to text messages based on regex matching.

```java
import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.robot.Robot;

public class Main {
  public static void main(String[] args) throws Exception {
    Robot r = Robot.from(ErisCasper.create());

    r.respond("ping", ctx -> ctx.reply("pong"));
    r.respond("echo (.*)", ctx -> ctx.reply(ctx.getGroup(1)));
  }
}
```

## Hear & Respond

The methods `Robot#hear` and `Robot#respond` take a regex and a Consumer that is executed upon a match.

`Robot#respond` will check for messages addressed directly to the bot via a ping, via the bots name, or prefixed by `+`.

For example, if your bot is named ErisCasper, the following are messages addressed to the bot:

  * +how are you?
  * ErisCasper how are you?
  * @ErisCasper how are you?

`Robot#hear` behaves the same as `Robot#respond`, but will check all messages (not just those addressed at the bot).

## Send & Reply

The method `Context#send` and `Context#reply` allow a bot to send responses.
`Context#reply` will ping the user who sent the message that triggered the bot.
`Context#send` will simply send the text specified.

`Context#getGroup` allows the use of text exctracted by groups in the regex used to trigger the bot.



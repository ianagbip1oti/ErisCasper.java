# Robot API

The Robot API presents a regex based API for detecting and responding to messages.

It is based upon the scripting API used in [Hubot](https://github.com/hubotio/hubot).

## An Example

[`PingAndEchoRobot`](https://github.com/princesslana/ErisCasper.java/blob/master/src/main/java/com/github/princesslana/eriscasper/examples/robot/PingAndEchoRobot.java)
provides a runnable example of using the Robot API. To run it:

```bash
> EC_TOKEN=your_bot_token mvn compile exec:java -Dexec.mainClass=com.github.princesslana.eriscasper.examples.robot.PingAndEchoRobot
```

## Hearing and Responding

A Robot can:

  * `hear` messages in a room, or
  * `respond` to messages sent directly to it.

Each method requires a regex and a function to generate a response.
For `hear` the regex will be checked against every message sent.
With `respond` the robot will check the regex if the message starts with '+', the robot's
username, or a mention of the robot.

```java
robot.hear("ping", ctx -> {
  // called whenever 'ping' is sent in chat
});

robot.respond("ping", ctx -> {
  // called for messages '+ping', 'BotName ping', or '@BotName ping'
});
```

## Send and Reply

The robot can:

  * `send` a replay, or
  * `reply` directly to a user.

With `reply` the user who's message is being replied to will be pinged.

```java
// sends 'pong' to the chat
robot.hear("ping", ctx -> ctx.send("pong"));

// replies '@User pong' to the user that sent ping
robot.respond("ping", ctx -> ctx.reply("pong"));
```

## Capturing Data

The regex provied to `hear` or `respond` can include capturing groups.
The values that are captured by these groups can be accesed via
RobotContext#match.

```java
// captures data from the message and uses it in the reply
robot.listen("echo (.+)", ctx -> ctx.reply(ctx.match(1)));
```





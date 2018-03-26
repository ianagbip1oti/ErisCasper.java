package com.github.princesslana.eriscasper.examples.robot;

import com.github.princesslana.eriscasper.api.robot.Robot;

public class PingAndEchoRobot {

  public static void main(String[] args) {
    Robot robot = new Robot();

    robot.listen("ping", ctx -> ctx.reply("pong"));
    robot.listen("echo (.+)", ctx -> ctx.reply(ctx.match(1)));

    robot.run();
  }
}

package igorekpotworek.cron;

import igorekpotworek.cron.CronExpression.Command;
import igorekpotworek.cron.CronExpression.Time;

import static org.apache.commons.lang3.StringUtils.rightPad;

public class CronExpressionFormatter {

  public static final int PAD_SIZE = 14;

  public String mkString(CronExpression expression) {
    return String.join(
        "\n",
        mkString("minute", expression.getMinutes()),
        mkString("hour", expression.getHours()),
        mkString("day of month", expression.getDayOfMonth()),
        mkString("month", expression.getMonth()),
        mkString("day of week", expression.getDayOfWeek()),
        mkString("command", expression.getCommand()));
  }

  private String mkString(String name, Time time) {
    return rightPad(name, PAD_SIZE) + time.getAllowedValues().toSortedSet().mkString(" ");
  }

  private String mkString(String name, Command command) {
    return rightPad(name, PAD_SIZE) + command.getValue();
  }
}

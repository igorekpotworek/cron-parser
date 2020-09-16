package igorekpotworek.cron;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Try;
import lombok.Value;
import lombok.val;

import static igorekpotworek.cron.ParserFactory.*;
import static io.vavr.API.For;
import static io.vavr.control.Try.failure;

@Value
public class CronExpression {

  Time minutes;
  Time hours;
  Time dayOfMonth;
  Time month;
  Time dayOfWeek;
  Command command;

  public static Try<CronExpression> parse(String expression) {
    val i = expression.lastIndexOf(" ");
    if (i == -1) {
      return failure(new CronExpressionParseException());
    }

    val timePart = expression.substring(0, i);
    val commandPart = expression.substring(i + 1);

    val normalisedTimePart =
        aliases().find(it -> it.matches(timePart)).map(Alias::getExpression).getOrElse(timePart);

    val timeTokens = normalisedTimePart.split(" ");
    if (timeTokens.length != 5) {
      return failure(new CronExpressionParseException());
    }

    return For(
            minutesParser().parse(timeTokens[0]),
            hoursParser().parse(timeTokens[1]),
            dayOfMonthParser().parse(timeTokens[2]),
            monthParser().parse(timeTokens[3]),
            dayOfWeekParser().parse(timeTokens[4]),
            commandParser().parse(commandPart))
        .yield(CronExpression::new);
  }

  @Value
  public static class Time {
    Set<Integer> allowedValues;

    public static Time empty() {
      return new Time(HashSet.empty());
    }
  }

  @Value
  public static class Command {
    String value;
  }
}

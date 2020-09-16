package igorekpotworek.cron;

import lombok.val;

public class CronParserApp {

  private static final CronExpressionFormatter CRON_EXPRESSION_FORMATTER =
      new CronExpressionFormatter();

  public static void main(String[] args) {
    val expression = CronExpression.parse(args[0]);
    System.out.println(
        expression
            .map(CRON_EXPRESSION_FORMATTER::mkString)
            .getOrElse("Expression cannot be parsed. Please provide valid cron expression"));
  }
}

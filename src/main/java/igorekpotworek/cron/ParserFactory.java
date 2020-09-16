package igorekpotworek.cron;

import igorekpotworek.cron.CronExpression.Time;
import igorekpotworek.cron.TokenParser.CommandParser;
import igorekpotworek.cron.TokenParser.NormalisedParser;
import igorekpotworek.cron.TokenParser.TimeUnitParser;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import lombok.val;

import static org.apache.commons.lang3.Range.between;

class ParserFactory {

  static TokenParser<Time> minutesParser() {
    return new TimeUnitParser(between(0, 59));
  }

  static TokenParser<Time> hoursParser() {
    return new TimeUnitParser(between(0, 23));
  }

  static TokenParser<Time> dayOfMonthParser() {
    return new TimeUnitParser(between(1, 31));
  }

  static TokenParser<Time> monthParser() {
    val substitutions =
        HashMap.of("JAN", "1")
            .put("FEB", "2")
            .put("MAR", "3")
            .put("APR", "4")
            .put("MAY", "5")
            .put("JUN", "6")
            .put("JUL", "7")
            .put("AUG", "8")
            .put("SEP", "9")
            .put("OCT", "10")
            .put("NOV", "11")
            .put("DEC", "12");
    return new NormalisedParser<>(new TimeUnitParser(between(1, 12)), substitutions);
  }

  static TokenParser<Time> dayOfWeekParser() {
    val substitutions =
        HashMap.of("SUN", "0")
            .put("MON", "1")
            .put("TUE", "2")
            .put("WED", "3")
            .put("THU", "4")
            .put("FRI", "5")
            .put("SAT", "6");
    return new NormalisedParser<>(new TimeUnitParser(between(0, 6)), substitutions);
  }

  static CommandParser commandParser() {
    return new CommandParser();
  }

  static List<Alias> aliases() {
    return List.of(
        new Alias("0 0 1 1 *", "@yearly", " @annually"),
        new Alias("0 0 1 * *", "@monthly"),
        new Alias("0 0 * * 0", "@weekly"),
        new Alias("0 0 * * *", "@daily", "@midnight"),
        new Alias("0 * * * *", "@hourly"));
  }
}

package igorekpotworek.cron;

import igorekpotworek.cron.CronExpression.Command;
import igorekpotworek.cron.CronExpression.Time;
import io.vavr.Tuple;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Try;
import lombok.Value;
import lombok.val;
import org.apache.commons.lang3.Range;

import static igorekpotworek.cron.CronExpression.Time.empty;
import static io.vavr.API.For;
import static io.vavr.collection.HashSet.rangeClosed;
import static io.vavr.collection.HashSet.rangeClosedBy;
import static io.vavr.control.Option.some;
import static io.vavr.control.Try.success;
import static java.lang.Integer.parseInt;

public interface TokenParser<T> {

  Try<T> parse(String value);

  @Value
  class AsteriskParser implements TokenParser<Time> {
    Range<Integer> range;

    @Override
    public Try<Time> parse(String value) {
      return some(value)
          .filter("*"::equals)
          .map(__ -> new Time(rangeClosed(range.getMinimum(), range.getMaximum())))
          .toTry(CronExpressionParseException::new);
    }
  }

  @Value
  class RangeParser implements TokenParser<Time> {
    Range<Integer> range;

    @Override
    public Try<Time> parse(String value) {
      return some(value)
          .filter(it -> it.matches("\\d+-\\d+"))
          .map(it -> it.split("-"))
          .map(it -> Tuple.of(parseInt(it[0]), parseInt(it[1])))
          .filter(it -> range.contains(it._1) && range.contains(it._2) && it._1 <= it._2)
          .map(it -> new Time(rangeClosed(it._1, it._2)))
          .toTry(CronExpressionParseException::new);
    }
  }

  @Value
  class EnumerationParser implements TokenParser<Time> {
    Range<Integer> range;

    @Override
    public Try<Time> parse(String value) {
      return some(value)
          .filter(it -> it.matches("(\\d+,)*\\d+"))
          .map(it -> HashSet.of(it.split(",")).map(Integer::parseInt))
          .filter(it -> it.forAll(range::contains))
          .map(Time::new)
          .toTry(CronExpressionParseException::new);
    }
  }

  @Value
  class StepParser implements TokenParser<Time> {
    TokenParser<Time> parser;
    Range<Integer> range;

    @Override
    public Try<Time> parse(String value) {
      return some(value)
          .filter(it -> it.matches(".+/\\d+"))
          .map(it -> it.split("/"))
          .map(it -> Tuple.of(parser.parse(it[0]), parseInt(it[1])))
          .filter(it -> range.contains(it._2))
          .toTry(CronExpressionParseException::new)
          .flatMap(it -> it._1.map(t -> createStepTime(it._2, t)));
    }

    private Time createStepTime(Integer step, Time time) {
      val allowedValues = time.getAllowedValues();
      return For(allowedValues.min(), allowedValues.max())
          .yield((min, max) -> new Time(rangeClosedBy(min, max, step).intersect(allowedValues)))
          .getOrElse(empty());
    }
  }

  @Value
  class CompositeParser<T> implements TokenParser<T> {
    List<TokenParser<T>> parsers;

    @Override
    public Try<T> parse(String value) {
      return parsers
          .flatMap(it -> it.parse(value))
          .headOption()
          .toTry(CronExpressionParseException::new);
    }
  }

  @Value
  class TimeUnitParser implements TokenParser<Time> {
    CompositeParser<Time> parser;

    TimeUnitParser(Range<Integer> range) {
      val stepParsers = List.of(new AsteriskParser(range), new RangeParser(range));
      this.parser =
          new CompositeParser<>(
              stepParsers
                  .append(new EnumerationParser(range))
                  .append(new StepParser(new CompositeParser<>(stepParsers), range)));
    }

    @Override
    public Try<Time> parse(String value) {
      return parser.parse(value);
    }
  }

  @Value
  class NormalisedParser<T> implements TokenParser<T> {
    TokenParser<T> parser;
    Map<String, String> substitution;

    private String normalise(String expression) {
      return substitution.foldLeft(expression, (e, s) -> e.replaceAll(s._1, s._2));
    }

    @Override
    public Try<T> parse(String value) {
      return parser.parse(normalise(value));
    }
  }

  class CommandParser implements TokenParser<Command> {

    @Override
    public Try<Command> parse(String value) {
      return success(new Command(value));
    }
  }
}

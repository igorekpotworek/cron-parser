package igorekpotworek.cron;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import lombok.Value;

@Value
class Alias {

  String expression;
  Set<String> aliases;

  Alias(String expression, String... alias) {
    this.aliases = HashSet.of(alias);
    this.expression = expression;
  }

  boolean matches(String expression) {
    return aliases.contains(expression);
  }
}

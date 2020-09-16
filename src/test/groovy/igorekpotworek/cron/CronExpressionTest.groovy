package igorekpotworek.cron


import spock.lang.Specification
import spock.lang.Unroll

class CronExpressionTest extends Specification {

    def expressionPrinter = new CronExpressionFormatter()

    def "should return proper time table for expression"() {
        given:
        def input = "*/15 0 1,15 * 1-5 /usr/bin/find"

        when:
        def expression = CronExpression.parse(input)

        then:
        expression.isSuccess()

        when:
        def result = expressionPrinter.mkString(expression.get())

        then:
        def expected = '''\
minute        0 15 30 45
hour          0
day of month  1 15
month         1 2 3 4 5 6 7 8 9 10 11 12
day of week   1 2 3 4 5
command       /usr/bin/find'''
        result == expected
    }

    def "should return proper time table for expression with step for a range"() {
        given:
        def input = "1-46/15 0 1,15 * 1-5 /usr/bin/find"

        when:
        def expression = CronExpression.parse(input)

        then:
        expression.isSuccess()

        when:
        def result = expressionPrinter.mkString(expression.get())

        then:
        def expected = '''\
minute        1 16 31 46
hour          0
day of month  1 15
month         1 2 3 4 5 6 7 8 9 10 11 12
day of week   1 2 3 4 5
command       /usr/bin/find'''
        result == expected
    }


    def "should return proper time table for expression with aliased day of week"() {
        given:
        def input = "*/15 0 1,15 * MON-FRI /usr/bin/find"

        when:
        def expression = CronExpression.parse(input)

        then:
        expression.isSuccess()

        when:
        def result = expressionPrinter.mkString(expression.get())

        then:
        def expected = '''\
minute        0 15 30 45
hour          0
day of month  1 15
month         1 2 3 4 5 6 7 8 9 10 11 12
day of week   1 2 3 4 5
command       /usr/bin/find'''
        result == expected
    }

    def "should return proper time table for expression with aliased month"() {
        given:
        def input = "*/15 0 1,15 JAN 1-5 /usr/bin/find"

        when:
        def expression = CronExpression.parse(input)

        then:
        expression.isSuccess()

        when:
        def result = expressionPrinter.mkString(expression.get())

        then:
        def expected = '''\
minute        0 15 30 45
hour          0
day of month  1 15
month         1
day of week   1 2 3 4 5
command       /usr/bin/find'''
        result == expected
    }


    def "should return proper time table for expression with alias"() {
        given:
        def input = "@yearly /usr/bin/find"

        when:
        def expression = CronExpression.parse(input)

        then:
        expression.isSuccess()

        when:
        def result = expressionPrinter.mkString(expression.get())

        then:
        def expected = '''\
minute        0
hour          0
day of month  1
month         1
day of week   0 1 2 3 4 5 6
command       /usr/bin/find'''
        result == expected
    }

    @Unroll
    def "should return failure for not valid expression"(String input) {
        when:
        def result = CronExpression.parse(input)

        then:
        result.isFailure()

        where:
        input                               | _
        "wrong input"                       | _
        "1 */15 0 1,15 * 1-5 /usr/bin/find" | _
        "*/15 0 1,15 * 1-5"                 | _
        "*/15 0 1,15 1-5 /usr/bin/find"     | _
        "*/60 0 1,15 1-5 /usr/bin/find"     | _
        "*/15 -1 1,15 * 1-5 /usr/bin/find"  | _
        "*/15 0 1,15 * -5 /usr/bin/find"    | _
        "/15 0 1,15 * 1-5 /usr/bin/find"    | _
        "1,2/15 0 1,15 * 1-5 /usr/bin/find" | _
        "*/15 0 1,15 * 1-7 /usr/bin/find"   | _
    }
}

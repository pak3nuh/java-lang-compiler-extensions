package io.github.pak3nuh.util.lang.enum_expression.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SingleValueTest {
    @Test
    void shouldNotGenerateAmbiguousCalls() {
        Integer eval1 = SingleValueExpression.eval(SingleValue.VALUE, () -> 1);
        Integer eval2 = SingleValueExpression.evalLambda(SingleValue.VALUE, () -> 2);
        assertEquals(1, eval1);
        assertEquals(2, eval2);
    }
}
package io.github.pak3nuh.util.lang.sealed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NestedTest {
    @Test
    void shouldEvaluateNestedTypes() {
        final Integer eval = NestedExpression.eval(new OuterChild.Child1(), new NestedExpression<Integer>() {
            @Override
            public Integer Child1(Nested.Child1 value) {
                return 1;
            }

            @Override
            public Integer Child2(Nested.Child2 value) {
                return 2;
            }

            @Override
            public Integer OuterChild(OuterChild value) {
                return 3;
            }

            @Override
            public Integer Child1(OuterChild.Child1 value) {
                return 4;
            }
        });
        assertEquals(4, eval);
    }

    @Test
    void shouldEvaluateLambda() {
        final Integer eval = NestedExpression.evalLambda(new OuterChild.Child1(),
                (Nested.Child1 item) -> 1,
                (Nested.Child2 item) -> 2,
                (OuterChild item) -> 3,
                (OuterChild.Child1 item) -> 4
        );
        assertEquals(4, eval);
    }
}

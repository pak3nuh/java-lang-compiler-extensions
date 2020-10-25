package io.github.pak3nuh.util.lang.enum_expression.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InnerEnumTest {
    @Test
    void shouldGenerate() {
        final Integer eval = TypeExpression.eval(InnerEnum.Type.T2, new TypeExpression<Integer>() {
            @Override
            public Integer T1() {
                return 1;
            }

            @Override
            public Integer T2() {
                return 2;
            }

            @Override
            public Integer T3() {
                return 3;
            }
        });
        assertEquals(2, eval);
    }

    @Test
    void shouldCreateBuilder() {
        final Integer result = TypeExpression.ExpressionBuilder.<Integer>create()
                .T1(() -> 1)
                .T2(() -> 2)
                .T3(() -> 3)
                .evaluator(InnerEnum.Type.T3);
        assertEquals(3, result);
    }
}

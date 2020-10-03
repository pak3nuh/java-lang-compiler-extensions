package io.github.pak3nuh.util.lang.enum_expression.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InnerWithCustomNameTest {
    @Test
    void shouldEval() {
        final Integer eval = MyCustomExpression.eval(InnerWithCustomName.Type.T2, new MyCustomExpression<Integer>() {
            @Override
            public Integer T1() {
                return 1;
            }

            @Override
            public Integer T2() {
                return 2;
            }
        });
        assertEquals(2, eval);
    }
}

package pt.pak3nuh.util.lang.enum_expression.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class JavaObjectTypeTest {
    @Test
    public void shouldGenerateVisitor() {
        final String eval = JavaObjectTypeExpression.eval(JavaObjectType.JAVA_3, new JavaObjectTypeExpression<String>() {
            @Override
            public String JAVA_1() {
                return "JAVA_1";
            }

            @Override
            public String JAVA_2() {
                return "JAVA_2";
            }

            @Override
            public String JAVA_3() {
                return "JAVA_3";
            }

            @Override
            public String JAVA_4() {
                return "JAVA_4";
            }
        });
        assertEquals("JAVA_3", eval);
    }

    @Test
    public void shouldGenerateVisitorLambda() {
        final Integer eval = JavaObjectTypeExpression.eval(JavaObjectType.JAVA_2,
                () -> 1,
                () -> 2,
                () -> 3,
                () -> 4);
        assertEquals(2, eval);
    }

    @Test
    void shouldThrowNpeOnNull() {
        assertThrows(NullPointerException.class, () -> {
            JavaObjectTypeExpression.eval(null,
                    () -> 1,
                    () -> 2,
                    () -> 3,
                    () -> 4);
        });
    }

    @Test
    void shouldReturnDefaults() {
        for (JavaObjectType value : JavaObjectType.values()) {
            String result = JavaObjectTypeExpression.eval(value, new JavaObjectTypeExpression.WithDefault<String>() {
                @Override
                public String defaultValue() {
                    return "Default";
                }
            });
            assertEquals(result, "Default");
        }
    }
}

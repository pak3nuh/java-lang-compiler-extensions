package io.github.pak3nuh.util.lang.sealed.car;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarTest {
    @Test
    void shouldGenerateEvalCode() {
        final String eval = CarExpression.eval(new Nissan(), new CarExpression<String>() {
            @Override
            public String Nissan() {
                return "GTR";
            }

            @Override
            public String Fiat() {
                return "Punto";
            }
        });
        assertEquals("GTR", eval);
    }

    @Test
    void shouldThrowNpe() {
        assertThrows(NullPointerException.class, () -> CarExpression.eval(null, new CarExpression<Object>() {
            @Override
            public Object Nissan() {
                return null;
            }

            @Override
            public Object Fiat() {
                return null;
            }
        }));
    }

    @Test
    void shouldThrowIllegalState() {
        assertThrows(IllegalStateException.class, () -> CarExpression.eval(new Car() {
        }, new CarExpression<String>() {
            @Override
            public String Nissan() {
                return "GTR";
            }

            @Override
            public String Fiat() {
                return "Punto";
            }
        }));
    }
}

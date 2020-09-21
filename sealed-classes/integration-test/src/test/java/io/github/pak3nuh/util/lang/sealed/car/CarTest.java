package io.github.pak3nuh.util.lang.sealed.car;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarTest {
    @Test
    void shouldGenerateEvalCode() {
        final String eval = CarExpression.eval(new Nissan(), new CarExpression<String>() {
            @Override
            public String Nissan(Nissan value) {
                return value.model();
            }

            @Override
            public String Fiat(Fiat value) {
                return value.model();
            }
        });
        assertEquals("GTR", eval);
    }

    @Test
    void shouldThrowNpe() {
        assertThrows(NullPointerException.class, () -> CarExpression.eval(null, new CarExpression<Object>() {
            @Override
            public Object Nissan(Nissan value) {
                return null;
            }

            @Override
            public Object Fiat(Fiat value) {
                return null;
            }
        }));
    }

    @Test
    void shouldThrowIllegalState() {
        assertThrows(IllegalStateException.class, () -> CarExpression.eval(new Car() {
        }, new CarExpression<String>() {
            @Override
            public String Nissan(Nissan value) {
                return null;
            }

            @Override
            public String Fiat(Fiat value) {
                return null;
            }
        }));
    }
}

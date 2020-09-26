package io.github.pak3nuh.util.lang.sealed.car;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

            @Override
            public String Bmw(Bmw value) {
                return "BMW";
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

            @Override
            public Object Bmw(Bmw value) {
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

            @Override
            public String Bmw(Bmw value) {
                return null;
            }
        }));
    }

    @Test
    void shouldNestSealedHierarchies() {
        final Boolean result = CarExpression.eval(new BmwI8(), new CarExpression<Boolean>() {
            @Override
            public Boolean Nissan(Nissan value) {
                return false;
            }

            @Override
            public Boolean Fiat(Fiat value) {
                return false;
            }

            @Override
            public Boolean Bmw(Bmw value) {
                return BmwExpression.eval(value, new BmwExpression<Boolean>() {
                    @Override
                    public Boolean BmwI8(BmwI8 value) {
                        return true;
                    }

                    @Override
                    public Boolean BmwM3(BmwM3 value) {
                        return false;
                    }
                });
            }
        });
        assertTrue(result);
    }
}

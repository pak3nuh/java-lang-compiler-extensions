package io.github.pak3nuh.util.lang.enum_expression.test;

import io.github.pak3nuh.util.lang.enum_expression.Expression;

public interface InnerEnum {
    @Expression(expressionBuilder = true)
    enum Type {
        T1, T2, T3
    }

}

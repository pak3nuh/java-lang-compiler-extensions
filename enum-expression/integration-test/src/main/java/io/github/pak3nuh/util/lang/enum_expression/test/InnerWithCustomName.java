package io.github.pak3nuh.util.lang.enum_expression.test;

import io.github.pak3nuh.util.lang.enum_expression.Expression;

public interface InnerWithCustomName {
    @Expression(value = "MyCustomExpression")
    enum Type {
        T1, T2
    }
}

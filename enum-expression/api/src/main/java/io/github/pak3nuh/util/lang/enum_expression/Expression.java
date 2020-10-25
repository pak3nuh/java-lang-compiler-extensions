package io.github.pak3nuh.util.lang.enum_expression;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an element target for an expression visitor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Expression {
    /**
     * An alternative name for the expression interface.
     * <p>By default, the interface will have the same name as the enum with the <b>Expression</b> appended.</p>
     */
    String value() default "";

    /**
     * <p>If true creates and expression builder with lambda support for cleaner code.</p>
     */
    boolean expressionBuilder() default false;

    /**
     * If true generates an interface that treats unhandled branches as defaults.
     */
    boolean defaultInterface() default false;
}

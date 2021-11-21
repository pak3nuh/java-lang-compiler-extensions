package io.github.pak3nuh.util.lang.delegates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     Marks a type used for delegation.
 *     The generator will pick up the type marked with this annotation and generate an interface that
 *     delegates all abstract methods in the inheritance chain to a delegate that must be provided by it's implementors.
 * </p>
 * <p>
 *     There are two modes for delegation, default or bridge.
 *     When in bridge mode it generates an interface that allows to delegate each method to it's own type.
 *     This mode allows delegates to extend multiple interfaces, but has caveats when the same method signature
 *     exists on multiple interfaces.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface Delegate {

    /**
     * <p>
     *     When active, the generated interface will have multiple <code>delegateTo</code> methods, one for each
     *     interface the target type extends.
     * </p>
     * <p>
     *     Bridge interfaces will not be delegates themselves, meaning if they define abstract methods,
     *     them implementor of the generated interface will need to implement those methods.
     * </p>
     */
    boolean bridge() default false;
}

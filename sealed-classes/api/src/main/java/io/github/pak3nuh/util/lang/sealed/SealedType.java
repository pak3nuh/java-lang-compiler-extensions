package io.github.pak3nuh.util.lang.sealed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks class as part of a sealed hierarchy.</p>
 * <p>All sealed types must inherit from an abstract class, in the same package, with a single, package private
 * constructor, in order to enforce all constraints.</p>
 * <p>Accompanying every sealed hierarchy, a visitor expression is created to navigate for all known implementations.</p>
 * <p>By default, sealed hierarchies also seal the package in the jar file, but this can be overridden.</p>
 * @see SealedPackage
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface SealedType {
}

package io.github.pak3nuh.util.lang.sealed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Seals a package for extension in the jar MANIFEST
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PACKAGE})
public @interface SealedPackage {
    /**
     * <p>Seals the package in MANIFEST so that it cannot be extended outside the JAR file.
     * If the package is sealed, then only a package private constructor is allowed.</p>
     * <p>Marking this as false removes any assurance that other cannot increase the number of inheritors
     * of the base class. Use with caution.</p>
     */
    boolean value() default true;
}

package io.github.pak3nuh.util.lang.sealed;

@SealedType
public final class OuterChild extends Nested {

    @SealedType
    public static final class Child1 extends Nested {

    }
}

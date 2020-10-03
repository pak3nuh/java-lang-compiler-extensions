package io.github.pak3nuh.util.lang.sealed;

public abstract class Nested {

    Nested() {}

    @SealedType
    public static final class Child1 extends Nested {

    }

    @SealedType
    public static final class Child2 extends Nested {

    }

}

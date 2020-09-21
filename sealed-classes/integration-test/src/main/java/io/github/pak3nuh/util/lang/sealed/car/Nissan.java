package io.github.pak3nuh.util.lang.sealed.car;

import io.github.pak3nuh.util.lang.sealed.SealedType;

@SealedType
public final class Nissan extends Car {
    public String model() {
        return "GTR";
    }
}

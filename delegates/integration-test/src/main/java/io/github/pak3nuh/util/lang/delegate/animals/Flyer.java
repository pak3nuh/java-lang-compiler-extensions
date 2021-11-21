package io.github.pak3nuh.util.lang.delegate.animals;

import io.github.pak3nuh.util.lang.delegates.Delegate;

@Delegate
public interface Flyer {
    void fly();
    <T, R> R dive(T context);
}

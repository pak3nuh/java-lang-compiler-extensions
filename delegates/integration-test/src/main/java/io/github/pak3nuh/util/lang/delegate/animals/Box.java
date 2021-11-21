package io.github.pak3nuh.util.lang.delegate.animals;

import io.github.pak3nuh.util.lang.delegates.Delegate;

import java.util.function.Function;

@Delegate
public interface Box<T extends Flyer> {
    T get();
    void set(T t);

    <Y extends Flyer> Box<Y> mapTo(Function<T, Y> mapper);
}

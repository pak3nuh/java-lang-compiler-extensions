package io.github.pak3nuh.util.lang.delegate.animals;

import io.github.pak3nuh.util.lang.delegates.Delegate;

@Delegate(bridge = true)
public interface Pigeon extends Walker, Bird, Animal {
}

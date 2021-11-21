package io.github.pak3nuh.util.lang.delegate.collections;

import io.github.pak3nuh.util.lang.delegates.Delegate;

import java.util.Map;

@Delegate(bridge = true)
public interface MyMap<K, V> extends Map<K, V>, Iterable<V> {
    boolean isOriginal();
}

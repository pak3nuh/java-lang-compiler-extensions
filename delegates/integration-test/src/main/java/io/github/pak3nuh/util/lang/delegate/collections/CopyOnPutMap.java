package io.github.pak3nuh.util.lang.delegate.collections;

import java.util.HashMap;
import java.util.Map;

public class CopyOnPutMap<K, V> implements MyMapBridge<K, V> {

    private Map<K, V> delegate;
    private boolean isOriginal = true;

    public CopyOnPutMap(Map<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<K, V> delegateTo(Map<K, V> caller) {
        return delegate;
    }

    @Override
    public V put(K arg0, V arg1) {
        if (isOriginal) {
            delegate = new HashMap<>(delegate);
            isOriginal = false;
        }
        return delegate.put(arg0, arg1);
    }

    @Override
    public Iterable<V> delegateTo(Iterable<V> caller) {
        return values();
    }

    @Override
    public boolean isOriginal() {
        return isOriginal;
    }
}

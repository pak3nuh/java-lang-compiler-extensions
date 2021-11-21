package io.github.pak3nuh.util.lang.delegate.animals;

import io.github.pak3nuh.util.lang.delegates.Delegate;

@Delegate
public interface Walker {
    void walk(String partner);
    int milesWalked();
}

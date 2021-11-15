package io.github.pak3nuh.util.lang.delegate.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class CopyOnPutMapTest {
    @Test
    void shouldCopyMap() {
        Map<String, String> original = new HashMap<>();
        original.put("1", "A");
        Map<String, String> strings = new CopyOnPutMap<>(original);

        Assertions.assertEquals("A", strings.get("1"));

        strings.put("2", "B");

        Assertions.assertEquals("B", strings.get("2"));
        Assertions.assertNull(original.get("2"));
    }
}
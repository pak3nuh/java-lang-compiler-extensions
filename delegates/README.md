# Delegate classes

Annotation processor to generate interfaces that reduce boilerplate needed for the
Delegate pattern.

Delegates are very useful for implementing other patterns like `Proxy`, object composition
or bridging.

## Usage

Import the artifacts using your build tool:
```kotlin
dependencies {
    implementation("io.github.pak3nuh.util.lang.compiler.delegates:api:$generatorVersion")
    annotationProcessor("io.github.pak3nuh.util.lang.compiler.delegates:processor:$generatorVersion")
}
```

Then just mark what interfaces you need the generator to write:
```java
@Delegate
interface Named {
    String name();
}

@Delegate
interface Greeter {
    String greet(String who);
}
```

The generator will create interfaces that implement all the boilerplate around the
pattern, allowing users to use it just by implementing one method to provide the
object it should delegate to:
```java
// Generated
interface NamedDelegate {
    Named delegateTo(Named caller);

    default String name() {
        return delegateTo(this).name();
    }
}

// Generated
interface GreeterDelegate {
    Greeter delegateTo(Greeter caller);

    default String greet(String who) {
        return delegateTo(this).greet(who);
    }
}
```

Users only need to implement the generated interfaces.
```java
class Person implements NamedDelegate, GreeterDelegate {
    @Override
    Named delegateTo(Named caller) {
        // return the object to delegate to Named calls
    }

    @Override
    Greeter delegateTo(Greeter caller) {
        // return the object to delegate to Greeter calls
    }
}
```

A user may still override each method individually for additional customization, for instance
auditing.

### Interface extension

The generator will stub every method in the inheritance chain that is marked as *abstract*, not
only in the interface with the `@Delegate` annotation.

## Bridges

Another specialization of delegates is when they are used as *bridges*.

Bridges allow a single implementor to respect multiple interfaces, acting as a middle man.

The generator supports bridging by specifying the `bridge` parameter. 
In this mode, the generated interface will contain multiple `delegateTo` methods, one for each interface to bridge.
This is specially useful on object composition patterns.

```java
@Delegate(bridge = true)
public interface Bird extends Flyer, Walker {}

// Generated
public interface BirdBridge extends Flyer, Walker {
    Flyer delegateTo(Flyer caller);
    Walker delegateTo(Walker caller);
    // all other methods
}
```

We can also use a bridge interface to delegate interfaces we don't control, for instance on the JDK.
```java
@Delegate(bridge = true)
public interface MyMap<K, V> extends Map<K, V> {}

// Generated
public interface MyMapBridge<K, V> extends Map<K, V> {
    Map<K, V> delegateTo(Map<K, V> caller);
    // all other methods
}
```

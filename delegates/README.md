# Delegate classes

Annotation processor to generate interfaces that reduce boilerplate needed for the
Delegate pattern.

Delegates are very useful for implementing other patterns like `Proxy`, object composition
or an approximation of `Mixins`.

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

interface Greeter {
    String greet(String who);
}
```

The generator will create interfaces that implement all the boilerplate around the
pattern, allowing users to use it just by implementing one method to switch the
object it delegates to:
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

## Interface extension

Currently, only methods that are on the interface marked with `@Delegate` will be picked up
on the code generator. This means that delegating a `Map<String,Integer>` is still not
possible.

This is a conscious decision for two main reasons:
1. Doing it is not that trivial because of parameter reifications. I would need capture type variables
and reify them based on the usage, which is not easy.
2. I expect this to be a small use case since most of the code generation is done on
code we control.

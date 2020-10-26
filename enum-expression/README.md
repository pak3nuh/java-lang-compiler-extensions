# Enum Expression

Annotation processor that generates code that allows to use enums as expressions and not statements
from older language versions.

A very useful compiler features on Java 14 is to use enums as expressions and
let the compiler assert the exhaustiveness of the code.

Since older language versions don't have this feature we can leverage code generation to accomplish the same.

## Usage

Import the artifacts using your build tool:
```kotlin
dependencies {
    implementation("io.github.pak3nuh.util.lang.compiler.enum-expression:api:$enumExprVersion")
    annotationProcessor("io.github.pak3nuh.util.lang.compiler.enum-expression:processor:$enumExprVersion")
}
```

Let's assume the following declaration
```java
@Expression
public enum Type {
    CAR, PLAIN, TRAIN
}
```

the processor than generates visitor like code that lets us define behavior for each constant:
```
interface TypeExpression<T> {
    T CAR();
    T PLAIN();
    T TRAIN();

    static <W> W eval(Type value, TypeExpression<W> delegate) {
        // checks each branch and call correct method
    } 

    static <W> W evalLamda(Type value, Supplier<T> CAR, Supplier<T> PLAIN, Supplier<T> TRAIN) {
        // checks each branch and call correct method
    } 
    ...
}
``` 

Once the `Type` declaration changes, compilation will break if the change is incompatible.

### Expression builders

Because the lambda evaluation method may generate issues due to the signature of all arguments is the same,
an expression builder can be generated.

```java
@Expression(expressionBuilder = true)
public enum Type {
    CAR, PLAIN, TRAIN
}

interface TypeExpression<T> {
    ...
    class ExpressionBuilder<T> {
        ...
    }
}
```

This builder has methods with lamda support and ensures that compilation will break if the enum definitions change 
in any way, improving security and code readability.

Auxiliary interfaces are generated to have a safe builder, so it is disabled by default.

### Unknown enum constants
While unlikely, since it is generated code, every unknown constant will throw `IllegalStateException`. A null
enum constant will result in an `NullPointerException`.

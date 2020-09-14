# Enum Expression

Annotation processor that generates code that allows to use enums as expressions and not statements
from older language versions.

A very useful compiler features on Java 14 is to use enums as expressions and
let the compiler assert the exhaustiveness of the code.

Since older language versions don't have this feature we can leverage code generation to accomplish the same.

## Usage

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
}
``` 

Once the `Type` declaration changes, compilation will break if the change is incompatible.

### Unknown enum constants
While unlikely, since it is generated code, every unknown constant will throw `IllegalStateException`. A null
enum constant will result in an `NullPointerException`.

# Sealed Classes
Restricts the inheritance chain of a class hierarchy so that the full tree can be known at compile time.
With the tree known at compile time, it can be possible to generate code that fully exhausts the
inheritance chain possibilities and breaks compilation if a branch is missing.

## Example
For a definition
```java
public abstract class Person {
    Person(){}
}

@SealedType
public final class Worker extends Person {
    String job;
}

@SealedType
public final class Student extends Person {
    String name;
}
```
the processor generates
```java
interface PersonExpression<T> {
    T Worker(Worker value);
    T Student(Student value);

    static <Y> Y eval(Person input, PersonExpression<Y> delegate) {
        // evaluates the input and invokes the correct delegate branch 
    }
    
    static <Y> Y evalLambda(Person input, Function<Worker, Y> worker, ...) {
        // evaluates the input and invokes the correct function
    }
}
```
begin possible to have multiple hierarchies, each with its own expression evaluator
```java
public abstract class Person {
    Person(){}
}

@SealedType
public abstract class Worker extends Person {
    Worker(){}
    String job;
}

@SealedType
public final class Teacher extends Worker {
    public Teacher() {
        job = "Teacher";
    }
    String assignedClass;
}

// Generated
interface PersonExpression<T> {}
interface WorkerExpression<T> {}
```
### Restrictions
There are some restrictions, enforced by the annotation processor, to ensure a minimum degree
of integrity on the generated code.

1. Any class annotated with `SealedType` must extend from an *abstract* class with only 
*package protected* or *private* constructors. The default constructor is not valid.
2. Any class annotated with `SealedType` must be either *final* or *abstract*.

These integrity checks, along with package sealing, ensure that the entire inheritance chain
is known in a single compilation step, not being possible to extend it further.

## Usage

Import the artifacts using your build tool:
```kotlin
dependencies {
    implementation("io.github.pak3nuh.util.lang.compiler.sealed:api:$version")
    annotationProcessor("io.github.pak3nuh.util.lang.compiler.sealed:processor:$version")
}
```

## Sealed Packages
By default, all packages are open, but we can seal them in the jar 
[manifest](https://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html#Manifest_Specification).

Unfortunately, from the code generator perspective, it is not possible to automate package sealing,
because building the manifest file is highly dependent on the build system.
Nevertheless, the processor will emmit to the generated source folder a *MANIFEST.MF* file with
the package sealing entries that can be appended to the final manifest file.

By default, all packages that a `SealedType` belongs to are implicitly marked as sealed, but is possible 
to force, or override, this by using the annotation `SealedPackage` on a `package.info` file.

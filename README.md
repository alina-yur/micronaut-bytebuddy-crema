# Micronaut + Byte Buddy + Crema

Minimal demo for a Micronaut app that asks Byte Buddy to generate bytecode and load a new class at run time.

The generated class is not present during the Native Image build. At run time, Byte Buddy defines `demo.crema.generated.RuntimeGreeting...`, Crema loads it, and AOT-compiled Micronaut application code invokes its generated `origin()` method through a method handle.

## Prerequisites

- GraalVM 25 with Native Image.
- A Native Image build that exposes `-H:+RuntimeClassLoading`.

Check the local image builder:

```shell
native-image --expert-options-all | grep RuntimeClassLoading
```

## JVM Smoke Test

```shell
mvn test
mvn -q compile exec:java
```

Expected output includes a generated class name and:

```text
Hello, Micronaut, from Byte Buddy runtime bytecode
```

## Native Image Without Crema

This builds a normal native image. The executable starts, but the Byte Buddy path is expected to fail when it tries to define the generated class.

```shell
mvn clean -Pnative-baseline -DskipTests package
./target/micronaut-bytebuddy-baseline Micronaut
```

Expected failure includes:

```text
Byte Buddy runtime class loading failed.
java.lang.IllegalStateException: Cannot load class class demo.crema.generated.RuntimeGreeting...
Caused by: java.lang.ClassNotFoundException: demo.crema.generated.RuntimeGreeting...
```

## Native Image With Crema

This profile adds `-H:+RuntimeClassLoading` to the Native Image build.

```shell
mvn clean -Pcrema -DskipTests package
./target/micronaut-bytebuddy-crema Micronaut
```

Expected output includes:

```text
Micronaut context: true
Generated class: demo.crema.generated.RuntimeGreeting...
Hello, Micronaut, from Byte Buddy runtime bytecode
```

## Why This Demo Is Small

Micronaut already avoids most reflection and proxy generation at run time by doing compile-time bean processing. Byte Buddy is included here specifically to show a library that generates JVM bytecode during application execution.

The demo uses `ClassLoadingStrategy.Default.WRAPPER`, so Byte Buddy defines the generated class through a dedicated class loader. In a regular native image that runtime class-loading step is the problem. In the Crema profile, Native Image is asked to keep support for loading and interpreting runtime classes.

The native build also initializes Byte Buddy at build time:

```xml
<buildArg>--initialize-at-build-time=net.bytebuddy</buildArg>
```

Without that, Byte Buddy's VM-version detection fails in this native-image setup before the demo reaches the runtime class-loading boundary.

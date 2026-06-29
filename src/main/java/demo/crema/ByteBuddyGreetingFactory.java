package demo.crema;

import io.micronaut.context.annotation.Requires;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;

@Singleton
@Requires(classes = ByteBuddy.class)
public final class ByteBuddyGreetingFactory {

    public GreetingResult createGreeting(String name) throws Throwable {
        Class<?> generatedType = new ByteBuddy(ClassFileVersion.JAVA_V25)
            .subclass(Object.class)
            .name("demo.crema.generated.RuntimeGreeting" + System.nanoTime())
            .defineMethod("origin", String.class, Visibility.PUBLIC)
            .intercept(FixedValue.value("Byte Buddy runtime bytecode"))
            .make()
            .load(RuntimeGreeting.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

        MethodHandle constructor = MethodHandles.publicLookup()
            .findConstructor(generatedType, MethodType.methodType(void.class));
        Object greeting = constructor.invoke();
        MethodHandle origin = MethodHandles.publicLookup()
            .findVirtual(generatedType, "origin", MethodType.methodType(String.class));
        return new GreetingResult(generatedType.getName(), RuntimeGreeting.message(name, (String) origin.invoke(greeting)));
    }
}

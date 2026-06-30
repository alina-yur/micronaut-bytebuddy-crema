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

    public GreetingResult createGreeting(PluginSpec plugin) throws Throwable {
        GeneratedMessage generated = createGeneratedMessage(plugin.message(), "ConferencePlugin");
        return new GreetingResult(generated.className(), generated.message());
    }

    private GeneratedMessage createGeneratedMessage(String messageText, String classPrefix) throws Throwable {
        Class<?> generatedType = new ByteBuddy(ClassFileVersion.JAVA_V25)
            .subclass(Object.class)
            .name("demo.crema.generated." + classPrefix + System.nanoTime())
            .defineMethod("message", String.class, Visibility.PUBLIC)
            .intercept(FixedValue.value(messageText))
            .make()
            .load(ByteBuddyGreetingFactory.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();

        MethodHandle constructor = MethodHandles.publicLookup()
            .findConstructor(generatedType, MethodType.methodType(void.class));
        Object greeting = constructor.invoke();
        MethodHandle message = MethodHandles.publicLookup()
            .findVirtual(generatedType, "message", MethodType.methodType(String.class));
        return new GeneratedMessage(generatedType.getName(), (String) message.invoke(greeting));
    }

    private record GeneratedMessage(String className, String message) {
    }
}

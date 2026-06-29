package demo.crema;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ByteBuddyGreetingFactoryTest {

    @Test
    void generatesGreetingClassAtRuntime() throws Throwable {
        try (ApplicationContext context = ApplicationContext.run()) {
            GreetingResult result = context.getBean(ByteBuddyGreetingFactory.class)
                .createGreeting("Micronaut");

            assertTrue(result.generatedClassName().startsWith("demo.crema.generated.RuntimeGreeting"));
            assertEquals("Hello, Micronaut, from Byte Buddy runtime bytecode", result.message());
        }
    }
}

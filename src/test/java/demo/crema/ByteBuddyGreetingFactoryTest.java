package demo.crema;

import io.micronaut.context.ApplicationContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ByteBuddyGreetingFactoryTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesGreetingClassAtRuntime() throws Throwable {
        try (ApplicationContext context = ApplicationContext.run()) {
            GreetingResult result = context.getBean(ByteBuddyGreetingFactory.class)
                .createGreeting("Micronaut");

            assertTrue(result.generatedClassName().startsWith("demo.crema.generated.RuntimeGreeting"));
            assertEquals("Hello, Micronaut, from Byte Buddy runtime bytecode", result.message());
        }
    }

    @Test
    void loadsConferencePluginProperties() throws IOException {
        Path pluginPath = tempDir.resolve("conference.properties");
        Files.writeString(pluginPath, """
            name=Conference Hall
            track=GraalVM Native Image
            speaker=Live stream desk
            """, StandardCharsets.UTF_8);

        PluginSpec plugin = PluginSpec.load(pluginPath);

        assertEquals("Conference Hall", plugin.name());
        assertEquals("GraalVM Native Image", plugin.track());
        assertEquals("Live stream desk", plugin.speaker());
        assertEquals(
            "conference plugin loaded after the native executable started (track: GraalVM Native Image, speaker: Live stream desk)",
            plugin.origin()
        );
    }

    @Test
    void generatesGreetingFromConferencePlugin() throws Throwable {
        PluginSpec plugin = new PluginSpec(
            "Conference Hall",
            "GraalVM Native Image",
            "Live stream desk"
        );

        try (ApplicationContext context = ApplicationContext.run()) {
            GreetingResult result = context.getBean(ByteBuddyGreetingFactory.class)
                .createGreeting(plugin);

            assertTrue(result.generatedClassName().startsWith("demo.crema.generated.ConferencePlugin"));
            assertEquals(
                "Hello, Conference Hall, from conference plugin loaded after the native executable started (track: GraalVM Native Image, speaker: Live stream desk)",
                result.message()
            );
        }
    }
}

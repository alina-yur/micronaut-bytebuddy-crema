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
    void loadsConferencePluginProperties() throws IOException {
        Path pluginPath = tempDir.resolve("conference.properties");
        Files.writeString(pluginPath, """
            name=Devoxx
            location=Belgium
            speaker=Alina
            """, StandardCharsets.UTF_8);

        PluginSpec plugin = PluginSpec.load(pluginPath);

        assertEquals("Devoxx", plugin.name());
        assertEquals("Belgium", plugin.location());
        assertEquals("Alina", plugin.speaker());
        assertEquals(
            "hello from Alina at Devoxx Belgium! Let's talk about what's latest and greatest in GraalVM :)",
            plugin.message()
        );
    }

    @Test
    void generatesGreetingFromConferencePlugin() throws Throwable {
        PluginSpec plugin = new PluginSpec(
            "Devoxx",
            "Belgium",
            "Alina"
        );

        try (ApplicationContext context = ApplicationContext.run()) {
            GreetingResult result = context.getBean(ByteBuddyGreetingFactory.class)
                .createGreeting(plugin);

            assertTrue(result.generatedClassName().startsWith("demo.crema.generated.ConferencePlugin"));
            assertEquals(
                "hello from Alina at Devoxx Belgium! Let's talk about what's latest and greatest in GraalVM :)",
                result.message()
            );
        }
    }
}

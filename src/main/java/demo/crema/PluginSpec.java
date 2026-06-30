package demo.crema;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record PluginSpec(String name, String location, String speaker) {

    private static final String DEFAULT_NAME = "Conference";
    private static final String DEFAULT_SPEAKER = "the conference speaker";
    private static final String GRAALVM_MESSAGE = "! Let's talk about what's latest and greatest in GraalVM :)";

    public PluginSpec {
        name = clean(name, DEFAULT_NAME);
        location = clean(location, "");
        speaker = clean(speaker, "");
    }

    public static PluginSpec load(Path path) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return new PluginSpec(
            properties.getProperty("name"),
            properties.getProperty("location"),
            properties.getProperty("speaker")
        );
    }

    public String message() {
        StringBuilder message = new StringBuilder("hello from ")
            .append(speaker.isBlank() ? DEFAULT_SPEAKER : speaker)
            .append(" at ")
            .append(name);
        if (!location.isBlank()) {
            message.append(' ').append(location);
        }
        return message.append(GRAALVM_MESSAGE).toString();
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}

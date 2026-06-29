package demo.crema;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public record PluginSpec(String name, String track, String speaker) {

    private static final String DEFAULT_NAME = "Conference Hall";
    private static final String ORIGIN_MESSAGE = "conference plugin loaded after the native executable started";

    public PluginSpec {
        name = clean(name, DEFAULT_NAME);
        track = clean(track, "");
        speaker = clean(speaker, "");
    }

    public static PluginSpec load(Path path) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return new PluginSpec(
            properties.getProperty("name"),
            properties.getProperty("track"),
            properties.getProperty("speaker")
        );
    }

    public String origin() {
        List<String> details = new ArrayList<>();
        if (!track.isBlank()) {
            details.add("track: " + track);
        }
        if (!speaker.isBlank()) {
            details.add("speaker: " + speaker);
        }
        if (details.isEmpty()) {
            return ORIGIN_MESSAGE;
        }
        return ORIGIN_MESSAGE + " (" + String.join(", ", details) + ")";
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}

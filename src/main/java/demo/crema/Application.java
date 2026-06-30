package demo.crema;

import io.micronaut.context.ApplicationContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class Application {

    private Application() {
    }

    public static void main(String[] args) {
        DemoCommand command = DemoCommand.parse(args);

        try (ApplicationContext context = ApplicationContext.run()) {
            ByteBuddyGreetingFactory factory = context.getBean(ByteBuddyGreetingFactory.class);

            System.out.println("Micronaut context: " + context.isRunning());
            switch (command.mode()) {
                case PLUGIN -> printPluginGreeting(factory, command.pluginPath());
                case WATCH -> watchPlugin(factory, command.pluginPath());
            }
        } catch (IOException | IllegalArgumentException exception) {
            System.err.println("Demo input failed.");
            exception.printStackTrace(System.err);
            System.exit(2);
        } catch (Throwable throwable) {
            System.err.println("Byte Buddy runtime class loading failed.");
            throwable.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void watchPlugin(ByteBuddyGreetingFactory factory, Path pluginPath) throws Throwable {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        int reload = 1;

        while (true) {
            System.out.println("Reload: " + reload);
            printPluginGreeting(factory, pluginPath);
            System.out.println("Edit the plugin file, then press Enter to load a new runtime class. Ctrl-D exits.");
            if (input.readLine() == null) {
                return;
            }
            reload++;
        }
    }

    private static void printPluginGreeting(ByteBuddyGreetingFactory factory, Path pluginPath) throws Throwable {
        PluginSpec plugin = PluginSpec.load(pluginPath);
        GreetingResult result = factory.createGreeting(plugin);

        System.out.println("Plugin file: " + pluginPath.toAbsolutePath().normalize());
        printGreeting(result);
    }

    private static void printGreeting(GreetingResult result) {
        System.out.println("Generated class: " + result.generatedClassName());
        System.out.println(result.message());
    }

    private enum Mode {
        PLUGIN,
        WATCH
    }

    private record DemoCommand(Mode mode, Path pluginPath) {

        private static final Path DEFAULT_PLUGIN_PATH = Path.of("plugins/conference.properties");

        static DemoCommand parse(String[] args) {
            if (args.length == 0) {
                return plugin(DEFAULT_PLUGIN_PATH);
            }
            if ("--plugin".equals(args[0])) {
                return plugin(pathArgOrDefault(args, "--plugin"));
            }
            if ("--watch".equals(args[0])) {
                return watch(pathArgOrDefault(args, "--watch"));
            }
            if ("conference".equals(args[0])) {
                requireNoExtraArgs(args, "conference");
                return plugin(DEFAULT_PLUGIN_PATH);
            }
            if ("watch".equals(args[0])) {
                requireNoExtraArgs(args, "watch");
                return watch(DEFAULT_PLUGIN_PATH);
            }
            if (args.length == 1 && args[0].endsWith(".properties")) {
                return plugin(Path.of(args[0]));
            }
            throw new IllegalArgumentException("expected conference, watch, --plugin, --watch, or a .properties file path");
        }

        private static DemoCommand plugin(Path pluginPath) {
            return new DemoCommand(Mode.PLUGIN, pluginPath);
        }

        private static DemoCommand watch(Path pluginPath) {
            return new DemoCommand(Mode.WATCH, pluginPath);
        }

        private static Path pathArgOrDefault(String[] args, String flag) {
            if (args.length == 1) {
                return DEFAULT_PLUGIN_PATH;
            }
            if (args.length == 2) {
                return Path.of(args[1]);
            }
            throw new IllegalArgumentException(flag + " expects zero or one properties file path");
        }

        private static void requireNoExtraArgs(String[] args, String command) {
            if (args.length != 1) {
                throw new IllegalArgumentException(command + " does not take extra arguments");
            }
        }
    }
}

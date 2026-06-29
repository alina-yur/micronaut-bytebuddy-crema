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
                case NAME -> printGreeting(factory.createGreeting(command.name()));
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
        System.out.println("Plugin: " + plugin.name());
        printGreeting(result);
    }

    private static void printGreeting(GreetingResult result) {
        System.out.println("Generated class: " + result.generatedClassName());
        System.out.println(result.message());
    }

    private enum Mode {
        NAME,
        PLUGIN,
        WATCH
    }

    private record DemoCommand(Mode mode, String name, Path pluginPath) {

        private static final String DEFAULT_NAME = "Crema";

        static DemoCommand parse(String[] args) {
            if (args.length == 0) {
                return name(DEFAULT_NAME);
            }
            if ("--plugin".equals(args[0])) {
                requirePath(args, "--plugin");
                return plugin(Path.of(args[1]));
            }
            if ("--watch".equals(args[0])) {
                requirePath(args, "--watch");
                return watch(Path.of(args[1]));
            }
            if (args.length == 1 && args[0].endsWith(".properties")) {
                return plugin(Path.of(args[0]));
            }
            return name(String.join(" ", args));
        }

        private static DemoCommand name(String name) {
            return new DemoCommand(Mode.NAME, name, null);
        }

        private static DemoCommand plugin(Path pluginPath) {
            return new DemoCommand(Mode.PLUGIN, null, pluginPath);
        }

        private static DemoCommand watch(Path pluginPath) {
            return new DemoCommand(Mode.WATCH, null, pluginPath);
        }

        private static void requirePath(String[] args, String flag) {
            if (args.length != 2) {
                throw new IllegalArgumentException(flag + " expects exactly one properties file path");
            }
        }
    }
}

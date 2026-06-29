package demo.crema;

import io.micronaut.context.ApplicationContext;

public final class Application {

    private Application() {
    }

    public static void main(String[] args) {
        String name = args.length == 0 ? "Crema" : String.join(" ", args);

        try (ApplicationContext context = ApplicationContext.run()) {
            ByteBuddyGreetingFactory factory = context.getBean(ByteBuddyGreetingFactory.class);
            GreetingResult result = factory.createGreeting(name);

            System.out.println("Micronaut context: " + context.isRunning());
            System.out.println("Generated class: " + result.generatedClassName());
            System.out.println(result.message());
        } catch (Throwable throwable) {
            System.err.println("Byte Buddy runtime class loading failed.");
            throwable.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

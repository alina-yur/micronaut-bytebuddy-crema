package demo.crema;

public final class RuntimeGreeting {

    private RuntimeGreeting() {
    }

    public static String message(String name, String origin) {
        return "Hello, " + name + ", from " + origin;
    }
}

package utils;

public class Logger {

    // Private constructor to prevent instantiation
    public Logger() {
    }

    /**
     * Logs an informational message to the standard output.
     * @param message The message to be logged.
     */
    public static void info(String message) {
        System.out.println(message);
    }

    /**
     * Logs an error message to the standard error output.
     * @param message The message to be logged.
     */
    public static void error(String message) {
        System.err.println(message);
    }

    /**
     * Logs an informational message to the standard output, followed by a new line.
     * @param message The message to be logged.
     */
    public static void infoNewLine(String message) {
        System.out.println(message);
    }
}
package com.spectralogic.ds3cli.logging;

public class Logging {
    private Logging() {/* Logging should not be created. */}
    private final static Logger logger = new StdoutLoggerImpl();

    public static void setVerbose(final boolean value) {
        logger.setVerbose(value);
    }
    public static boolean isVerbose() {
        return logger.isVerbose();
    }
    public static void log(final String message) {
        logger.log(message);
    }
    public static void logf(final String message, final Object... args) {
        logger.logf(message, args);
    }

    public static interface Logger {
        public void setVerbose(final boolean value);
        public boolean isVerbose();
        public void log(final String message);
        public void logf(final String message, final Object... args);
    }
}

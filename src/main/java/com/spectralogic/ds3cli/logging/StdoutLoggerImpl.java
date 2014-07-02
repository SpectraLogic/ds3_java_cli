package com.spectralogic.ds3cli.logging;

class StdoutLoggerImpl implements Logging.Logger {
    private boolean verbose = false;

    @Override
    public void setVerbose(boolean value) {
        this.verbose = value;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void log(final String message) {
        if (verbose) System.out.println(message);
    }

    @Override
    public void logf(final String message, final Object... args) {
        if (verbose) System.out.printf(message + "\n", args);
    }
}

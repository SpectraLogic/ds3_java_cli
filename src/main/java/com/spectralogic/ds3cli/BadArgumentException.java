package com.spectralogic.ds3cli;

public class BadArgumentException extends Exception {

    public BadArgumentException(final String message, final Exception e) {
        super(message, e);
    }
}

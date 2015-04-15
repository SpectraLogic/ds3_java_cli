package com.spectralogic.ds3cli;

public class CommandResponse {
    private final String message;
    private final int returnCode;

    public CommandResponse(final String message, final int returnCode) {
        this.message = message;
        this.returnCode = returnCode;
    }

    public String getMessage() {
        return message;
    }

    public int getReturnCode() {
        return returnCode;
    }
}

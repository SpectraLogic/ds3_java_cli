package com.spectralogic.ds3cli;


public class CommandException extends Exception {
    public CommandException(final String errorMsg){
        super (errorMsg);
    }

    public CommandException( final String errorMsg, final Exception e ) {
        super (errorMsg, e);
    }
}

package com.spectralogic.ds3cli.views.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.logging.Logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CommandExceptionCliView implements View<CommandException> {
    @Override
    public String render(final CommandException obj) throws JsonProcessingException {
        final StringBuilder builder = new StringBuilder();
        builder.append(obj.getMessage());

        try {
            if (Logging.isVerbose()) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final PrintWriter pOut = new PrintWriter(out);
                obj.printStackTrace(pOut);
                builder.append('\n');
                builder.append(out.toString("utf-8"));
            }
        } catch (final UnsupportedEncodingException e) {
            System.out.println("ERROR: Failed to parse error stack trace as message, defaulting to standard error printing");
            obj.getCause().printStackTrace();
        }

        return builder.toString();
    }
}

package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.*;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.ServiceLoader;

public class GetConfigSummary extends CliCommand<DefaultResult> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private Arguments mainArgs;
    // command executed (user real class name, not underscore ormat)
    private final String[] commandList = {"systemInformation", "getDataPathBackend", "getTapeFailure", "GetSystemFailure", "GetCacheState", "GetCapacitySummary"  };

    public GetConfigSummary() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        // store a local ref for calling commands
        this.mainArgs = args;
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final StringBuilder result = new StringBuilder("CONFIGURATION SUMMARY\n");

        for (final String commandName : commandList ) {
            CliCommand command = getCommandExecutor(commandName);
            final View view = command.getView(ViewType.CLI);
            result.append(commandName);
            result.append("\n");
            result.append(view.render(command.init(this.mainArgs).call()));
            result.append("\n\n");
        }
        return new DefaultResult(result.toString());
    }

    private CliCommand getCommandExecutor(final String commandName) throws CommandException {

        final Iterator<CliCommand> implementations = getAllCommands();
        while (implementations.hasNext()) {
            final CliCommand implementation = implementations.next();
            final String className = implementation.getClass().getSimpleName();
            if (className.equalsIgnoreCase(commandName)) {
                return implementation.withProvider(getProvider(), getFileUtils());
            }
        }
        throw new CommandException("No command class: " + commandName);
    }

    private Iterator<CliCommand> getAllCommands() {
        final ServiceLoader<CliCommand> loader =
                ServiceLoader.load(CliCommand.class);
        return loader.iterator();
    }


}

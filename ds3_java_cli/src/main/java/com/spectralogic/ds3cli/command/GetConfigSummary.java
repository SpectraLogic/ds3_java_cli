package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.*;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetConfigSummaryResult;
import com.spectralogic.ds3cli.models.Result;

import java.text.SimpleDateFormat;
import java.util.*;

public class GetConfigSummary extends CliCommand<GetConfigSummaryResult> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final StringBuilder completeResult = new StringBuilder("CONFIGURATION SUMMARY\n");
    private final Map<String, Result> resultsMap = new HashMap<String, Result>();

    private ViewType viewType;
    private Arguments mainArgs;
    // commands to be executed (use real class name, not underscore format)
    private final String[] commandList = {"SystemInformation", "VerifySystemHealth", "GetDataPathBackend",
            "GetTapeFailure", "GetSystemFailure", "GetCacheState", "GetCapacitySummary" };

    public GetConfigSummary() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        // store a local ref for calling commands
        this.mainArgs = args;
        this.viewType = args.getOutputFormat();
        return this;
    }

    @Override
    public GetConfigSummaryResult call() throws Exception {
        // run all commands, store results in Map
        for (final String commandName : commandList ) {
            final CliCommand command = getCommandExecutor(commandName);
            final Result result =  (Result)command.init(this.mainArgs).call();
            resultsMap.put(commandName, result);
            if (viewType.equals(ViewType.CLI)) {
                final View view = command.getView(this.viewType);
                appendResult(commandName, view.render(result));
            }
        }
        return new GetConfigSummaryResult(resultsMap, completeResult.toString());
    }

    private void appendResult(final String title, final String result) {
        completeResult.append(title);
        completeResult.append("\n");
        completeResult.append(result);
        completeResult.append("\n\n");
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
        final ServiceLoader<CliCommand> loader = ServiceLoader.load(CliCommand.class);
        return loader.iterator();
    }

    @Override
    public View<GetConfigSummaryResult> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return new com.spectralogic.ds3cli.views.json.GetConfigSummaryView();
        }
        return new com.spectralogic.ds3cli.views.cli.GetConfigSummaryView();
    }
}

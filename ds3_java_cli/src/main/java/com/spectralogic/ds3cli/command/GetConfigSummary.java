/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.RawView;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.models.GetConfigSummaryResult;
import com.spectralogic.ds3cli.models.Result;
import com.spectralogic.ds3cli.views.cli.GetConfigSummaryView;
import com.spectralogic.ds3cli.views.json.DataView;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/***
 *  Run all the commands in this.commandList to collect a summary of status and configuration
 */
public class GetConfigSummary extends CliCommand<GetConfigSummaryResult> {

    private final StringBuilder completeResult = new StringBuilder("CONFIGURATION SUMMARY\n");
    private final Map<String, Result> resultsMap = new HashMap<>();

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
        // add platform info
        resultsMap.put("PlatformInformation", new DefaultResult(getPlatformInformation()));
        appendResult("PlatformInformation", getPlatformInformation());

        // run all commands, store results in Map
        for (final String commandName : commandList ) {
            final CliCommand command = getCommandExecutor(commandName);
            final Result result =  (Result)command.init(this.mainArgs).call();
            resultsMap.put(commandName, result);
            if (viewType.equals(ViewType.CLI)) {
                final RawView view = command.getView();
                try (final StringWriter writer = new StringWriter()) {
                    view.renderToStream(writer, result);
                    final String resultMessage = writer.toString();
                    appendResult(commandName, resultMessage);
                }
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
                return implementation.withProvider(getProvider(), getFileSystemProvider());
            }
        }
        throw new CommandException("No command class: " + commandName);
    }

    private Iterator<CliCommand> getAllCommands() {
        final ServiceLoader<CliCommand> loader = ServiceLoader.load(CliCommand.class);
        return loader.iterator();
    }

    @Override
    public View<GetConfigSummaryResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetConfigSummaryView();
    }
}

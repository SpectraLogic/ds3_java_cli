/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
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
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.models.Result;
import com.spectralogic.ds3cli.util.CommandHelpText;
import com.spectralogic.ds3cli.util.CommandListener;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileSystemProvider;
import com.spectralogic.ds3cli.views.cli.DefaultView;
import com.spectralogic.ds3cli.views.json.StringView;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class CliCommand<T extends Result> implements Callable<T> {

    protected final static List<Option> EMPTY_LIST = Collections.emptyList();
    protected final List<CommandListener> listeners = new ArrayList<CommandListener>();

    private Ds3Provider ds3Provider;
    private FileSystemProvider fileSystemProvider;
    protected ViewType viewType = ViewType.CLI;

    // for service provider instantiation
    public CliCommand() {
        this.ds3Provider = null;
        this.fileSystemProvider = null;
    }

    public CliCommand withProvider(final Ds3Provider ds3Provider, final FileSystemProvider fileSystemProvider) {
        this.ds3Provider = ds3Provider;
        this.fileSystemProvider = fileSystemProvider;
        return this;
    }

    public CliCommand(final Ds3Provider ds3Provider, final FileSystemProvider fileSystemProvider) {
        this.ds3Provider = ds3Provider;
        this.fileSystemProvider = fileSystemProvider;
    }

    /**
     * parse command line args and create client
     * override this method to add command line Options
     * @param args Arguments object
     * @returns this
     * @throws Exception parsing and argument exceptions
     */
    public CliCommand init(final Arguments args) throws Exception {
        args.parseCommandLine();
        this.viewType = args.getOutputFormat();
        return this;
    }

    protected Ds3Client getClient() {
        return this.ds3Provider.getClient();
    }

    Ds3ClientHelpers getClientHelpers() {
        return this.ds3Provider.getClientHelpers();
    }

    Ds3Provider getProvider() {
        return this.ds3Provider;
    }

    FileSystemProvider getFileSystemProvider() {
        return this.fileSystemProvider;
    }

    /**
     * Lookup help for '--help' COMMAND from resource file
     * (Override or add help text to resources/com/spectralogic/dscli/help.properties
     * @param command
     * @return
     */
    public static String getVerboseHelp(final String command) {
        return CommandHelpText.getHelpText(command.toUpperCase());
    }

    /**
     * Convenience method to add args, parse, and get output-format
     * @param requiredArgs List of options to be added as required use EMPTY_LIST for none
     * @param optionalArgs List of options to be added as optional use EMPTY_LIST for none
     * @param args
     * @throws BadArgumentException
     * @throws ParseException
     */
    protected void processCommandOptions(final List<Option> requiredArgs,
                                         final List<Option> optionalArgs,
                                         final Arguments args) throws BadArgumentException, ParseException {
        addRequiredArguments(requiredArgs, args);
        addOptionalArguments(optionalArgs, args);
        args.parseCommandLine();
        this.viewType = args.getOutputFormat();
    }


    /**
     * Inits COMMAND to add all options, then prints usage description
     * @param arguments Arguments object
     */
    public void printArgumentHelp(final Arguments arguments) {
        try {
            // init to install options
            init(arguments);
        } catch (final Exception e) {
            // does not have to parse to print help
        }
        finally {
            arguments.printHelp();
        }
    }

    /**
     * Add each Option in a list as a required argument
     * @param reqArgs List<Option> of required args
     * @param args Arguments object
     */
    void addRequiredArguments(final List<Option> reqArgs, final Arguments args) {
        for (final Option oReq : reqArgs ) {
            oReq.setRequired(true);
            args.addOption(oReq, oReq.getArgName());
        }
    }

    /**
     * Add each Option in a list as an optional argument
     * @param reqArgs List<Option> of optional args
     * @param args Arguments object
     */
    private void addOptionalArguments(final List<Option> reqArgs, final Arguments args) {
        for (final Option oOpt : reqArgs ) {
            oOpt.setRequired(false);
            args.addOption(oOpt, oOpt.getArgName());
        }
    }

    /**
     * get the appropriate View to parse and format response
     * @return The View which formats the response.
     */
    public View<T> getView() {
        if (this.viewType == ViewType.JSON) {
            return new StringView<>();
        }
        return new DefaultView<>();
    }

    /**
     * Compose and send request, parse and format response
     * @return CommandResponse with status and message
     * @throws Exception
     */
    public CommandResponse render() throws Exception {
        final String message = getView().render(call());
        return new CommandResponse(message, 0);
    }

    public static String getPlatformInformation() {
        return String.format("Java Version: {%s}\n", System.getProperty("java.version"))
            + String.format("Java Vendor: {%s}\n", System.getProperty("java.vendor"))
            + String.format("JVM Version: {%s}\n", System.getProperty("java.vm.version"))
            + String.format("JVM Name: {%s}\n", System.getProperty("java.vm.name"))
            + String.format("OS: {%s}\n", System.getProperty("os.name"))
            + String.format("OS Arch: {%s}\n", System.getProperty("os.arch"))
            + String.format("OS Version: {%s}\n", System.getProperty("os.version"));
    }

    public void registerListener(final CommandListener listen) {
        listeners.add(listen);
    }

    protected void broadcast(final String message) {
        for (final CommandListener listener : listeners) {
            listener.append(message);
        }
    }

}

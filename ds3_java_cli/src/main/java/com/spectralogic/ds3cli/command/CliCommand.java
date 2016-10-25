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

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.Result;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.views.cli.CommandExceptionCliView;
import com.spectralogic.ds3cli.views.cli.DefaultView;
import com.spectralogic.ds3cli.views.json.CommandExceptionJsonView;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.spectralogic.ds3cli.util.CommandHelpText;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public abstract class CliCommand<T extends Result> implements Callable<T> {

    private Ds3Provider ds3Provider;
    private FileUtils fileUtils;
    protected ViewType viewType = ViewType.CLI;
    protected final static List<Option> EMPTY_LIST = Collections.emptyList();

    // for service provider instantiation
    public CliCommand() {
        this.ds3Provider = null;
        this.fileUtils = null;
    }

    public CliCommand withProvider(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        this.ds3Provider = ds3Provider;
        this.fileUtils = fileUtils;
        return this;
    }

    public CliCommand(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        this.ds3Provider = ds3Provider;
        this.fileUtils = fileUtils;
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

    protected Ds3ClientHelpers getClientHelpers() {
        return this.ds3Provider.getClientHelpers();
    }

    protected Ds3Provider getProvider() {
        return this.ds3Provider;
    }

    protected FileUtils getFileUtils() {
        return this.fileUtils;
    }

    /**
     * Lookup help for '--help' COMMAND from resource file
     * (Override or add help text to resources/com/spectralogic/dscli/help.properties
     * @param command
     * @return
     */
    public static String getLongHelp(final String command) {
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
    protected void addRequiredArguments(final List<Option> reqArgs, final Arguments args) {
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
    protected void addOptionalArguments(final List<Option> reqArgs, final Arguments args) {
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
            return (View<T>) new com.spectralogic.ds3cli.views.json.DefaultView();
        }
        return (View<T>) new DefaultView();
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

    public ViewType getOutputFormat() {
        return this.viewType;
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

}

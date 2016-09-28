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

import com.spectralogic.ds3cli.*;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.Result;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3cli.views.cli.CommandExceptionCliView;
import com.spectralogic.ds3cli.views.cli.DefaultView;
import com.spectralogic.ds3cli.views.json.CommandExceptionJsonView;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.concurrent.Callable;

import com.spectralogic.ds3cli.util.CommandHelpText;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.io.IOExceptionWithCause;

public abstract class CliCommand<T extends Result> implements Callable<T> {

    private Ds3Provider ds3Provider;
    private FileUtils fileUtils;
    protected Arguments args;
    protected ViewType viewType = ViewType.CLI;

    public CliCommand() {
        this.ds3Provider = null;
        this.fileUtils = null;
    }

    // TODO dlete -- this is just to build
    // new argument handling uses empty constructor and creates client in init()
    public CliCommand withProvider(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        this.ds3Provider = ds3Provider;
        this.fileUtils = fileUtils;
        return this;
    }
    // TODO delete -- this is just to build
    // new argument handling uses empty constructor and creates client in init()
    public CliCommand(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        this.ds3Provider = ds3Provider;
        this.fileUtils = fileUtils;
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
     * parse COMMAND line args and create client
     * override this method to add COMMAND line Options
     * @param args Arguments object
     * @returns this
     * @throws Exception parsing and argumnet exceptions
     */
    public CliCommand init(final Arguments args) throws Exception {
        args.parseCommandLine();
        this.viewType = args.getOutputFormat();
        return this;
    }

    /**
     * Add each Option in a list as a required argument
     * @param reqArgs List<Option> of required args
     * @param args Argumnets object
     */
    protected void addRequiredArguments(final List<Option> reqArgs, Arguments args) {
        for (final Option o : reqArgs ) {
            final Option oReq = o;
            oReq.setRequired(true);
            args.addOption(oReq, o.getArgName());
        }
    }

    /**
     * Add each Option in a list as an optional argument
     * @param reqArgs List<Option> of optional args
     * @param args Argumnets object
     */
    protected void addOptionalArguments(final List<Option> reqArgs, Arguments args) {
        for (final Option o : reqArgs ) {
            final Option oReq = o;
            oReq.setRequired(false);
            args.addOption(oReq, o.getArgName());
        }
    }

    /**
     * Set the client
     * @param provider Ds3Provider
     * @param utils FileUtils
     * @throws MissingOptionException if required parameter is not in args or environment vars
     */
    public void setClient(final Ds3Provider provider, final FileUtils utils) {
        this.ds3Provider = provider;
        this.fileUtils = utils;
    }

    protected void isCliSupported() throws SignatureException, IOException {
        if (!Utils.isVersionSupported(this.getClient())) {
            System.out.println(String.format("ERROR: Minimum Black Pearl supported is %s", Utils.MINIMUM_VERSION_SUPPORTED));
            System.exit(2);
        }
    }

    /**
     * Compose and send request, parse and format response
     * @return CommandResponse with status and message
     * @throws Exception
     */
    public CommandResponse render() throws Exception {
        final View view = getView();

        try {
            final String message = view.render(call());
            return new CommandResponse(message, 0);
        }
        catch(final CommandException e) {
            final String message;
            if (this.getOutputFormat() == ViewType.JSON) {
                message = new CommandExceptionJsonView().render(e);
            }
            else {
                message = new CommandExceptionCliView().render(e);
            }
            return new CommandResponse(message, 1);
        }
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
     * Inits COMMAND to add all options, then prints usage description
     * @param arguments Arguments object
     */
    public void printArgumentHelp(Arguments arguments) {
        try {
            // init to install options
            init(arguments);
        } catch (Exception e) {}
        finally {
            // does not have to parse
            arguments.printHelp();
        }
    }

    /**
     * get the appropriate View to parse and format response
     * @return
     */
    public View<T> getView() {
        if (this.viewType == ViewType.JSON) {
            return (View<T>) new com.spectralogic.ds3cli.views.json.DefaultView();
        }
        return (View<T>) new DefaultView();
    }

    public ViewType getOutputFormat() {
        return this.viewType;
    }

    /**
     * get VM and OS info for logging
     * @return String description of platform variables
     */
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

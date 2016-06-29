/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.command.*;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.views.cli.CommandExceptionCliView;
import com.spectralogic.ds3cli.views.json.CommandExceptionJsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

public class Ds3Cli implements Callable<CommandResponse> {

    private final static Logger LOG = LoggerFactory.getLogger(Ds3Cli.class);

    private final Arguments args;
    private final Ds3Provider ds3Provider;
    private final FileUtils fileUtils;

    public Ds3Cli(final Ds3Provider provider, final Arguments args, final FileUtils fileUtils) {
        this.args = args;
        this.ds3Provider = provider;
        this.fileUtils = fileUtils;
    }

    @Override
    public CommandResponse call() throws Exception {
        final CliCommand command = getCommandExecutor();

        final View view = command.getView(this.args.getOutputFormat());

        try {
            final String message = view.render(command.init(this.args).call());
            return new CommandResponse(message, 0);
        }
        catch(final CommandException e) {
            final String message;
            if (this.args.getOutputFormat() == ViewType.JSON) {
                message = new CommandExceptionJsonView().render(e);
            }
            else {
                message = new CommandExceptionCliView().render(e);
            }
            return new CommandResponse(message, 1);
        }
    }

    public CommandResponse getCommandHelp() throws Exception {
        final CliCommand command = getCommandExecutor();

        try {
            final String message = command.getLongHelp(this.args.getCommand());
            return new CommandResponse(message, 0);
        }
        catch(final Exception e) {
            final String message = "getCommandHelp failed";
            return new CommandResponse(message, 1);
        }
    }

    private CliCommand getCommandExecutor() throws CommandException {
        final CommandValue commandName = this.args.getCommand();

        String commandCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, commandName.toString());

        try {
            final Class<?> commandClass = Class.forName("com.spectralogic.ds3cli.command." + commandCamel);
            final Constructor<?> commandConstructor = commandClass.getConstructor(Ds3Provider.class, FileUtils.class);
            return (CliCommand) commandConstructor.newInstance(this.ds3Provider, this.fileUtils);
        } catch (ClassNotFoundException noClass) {
            throw new CommandException("No command class: " + commandName, noClass);
        } catch (Exception noConstruct) {
            throw new CommandException("Cannot create command: " + commandName, noConstruct);
        }
    }
}

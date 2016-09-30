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

import com.google.common.base.CaseFormat;
import com.spectralogic.ds3cli.exceptions.CommandException;

import java.util.Iterator;
import java.util.ServiceLoader;

public class CliCommandFactory {

    public static CliCommand getCommandExecutor(final String commandName) throws CommandException {
        final String commandCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, commandName.toString());

        final Iterator<CliCommand> implementations = getAllCommands();
        while (implementations.hasNext()) {
            final CliCommand implementation = implementations.next();
            final String className = implementation.getClass().getSimpleName();
            if (className.equalsIgnoreCase(commandCamel)) {
                return implementation;
            }
        }
        throw new CommandException("No command class: " + commandName);
    }

    public static String listAllCommands() {
        final StringBuilder commands = new StringBuilder("Installed Commands: ");
        final Iterator<CliCommand> implementations = getAllCommands();
        while (implementations.hasNext()) {
            final CliCommand implementation = implementations.next();
            commands.append(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, implementation.getClass().getSimpleName()));
            if (implementations.hasNext()) {
                commands.append(", ");
            }
        }
        return commands.toString();
    }

    private static Iterator<CliCommand> getAllCommands() {
        final ServiceLoader<CliCommand> loader =
                ServiceLoader.load(CliCommand.class);
        return loader.iterator();
    }


}




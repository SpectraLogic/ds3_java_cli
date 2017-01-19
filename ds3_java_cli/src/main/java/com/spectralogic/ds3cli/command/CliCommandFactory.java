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
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.spectralogic.ds3cli.api.exceptions.CommandException;

import javax.annotation.Nullable;
import java.util.ServiceLoader;

public final class CliCommandFactory {

    public static BaseCliCommand getCommandExecutor(final String commandName) throws CommandException {
        final String commandCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, commandName.toString());

        final Iterable<BaseCliCommand> implementations = getAllCommands();
        for  (final BaseCliCommand implementation : implementations) {
            final String className = implementation.getClass().getSimpleName();
            if (className.equalsIgnoreCase(commandCamel)) {
                return implementation;
            }
        }
        throw new CommandException("No command class: " + commandName);
    }

    public static String listAllCommands() {
        final StringBuilder commandHelp = new StringBuilder("Installed Commands: ");

        final FluentIterable<String> commands = FluentIterable.from(getAllCommands()).transform(
                new Function<BaseCliCommand, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable final BaseCliCommand input) {
                        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, input.getClass().getSimpleName());
                    }
                }
        );
        final Joiner joiner = Joiner.on(", ");
        commandHelp.append(joiner.join(commands));
        return commandHelp.toString();
    }

    private static Iterable<BaseCliCommand> getAllCommands() {
        return ServiceLoader.load(BaseCliCommand.class);
    }

}




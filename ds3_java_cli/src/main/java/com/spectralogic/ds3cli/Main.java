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

package com.spectralogic.ds3cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import com.google.common.base.Joiner;
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.CliCommandFactory;
import com.spectralogic.ds3cli.exceptions.*;
import com.spectralogic.ds3cli.util.*;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

import static com.spectralogic.ds3cli.ArgumentFactory.COMMAND;

public final class Main {

    private final static String PROPERTY_FILE = "ds3_cli.properties";

    // initialize and add appenders to root logger
    private final static ch.qos.logback.classic.Logger LOG =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final static String LOG_FORMAT_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} +++ %msg%n";
    private final static String LOG_ARCHIVE_FILE_PATTERN =  "spectra%i.log";
    private final static String LOG_DIR = "./ds3logs/";
    private final static String LOG_FILE_NAME = "spectra.log";

    // register exception handlers
    private final static Ds3ExceptionHandlerMapper EXCEPTION = Ds3ExceptionHandlerMapper.getInstance();
    static {
        EXCEPTION.addHandler(FailedRequestException.class, new FailedRequestExceptionHandler());
        EXCEPTION.addHandler(RuntimeException.class, new RuntimeExceptionHandler());
    }

    private static void configureLogging(final Level consoleLevel, final Level fileLevel) {

        final LoggerContext loggerContext = LOG.getLoggerContext();
        loggerContext.reset();

        // set root log to the most permissive filter (affects performance big time (JAVACLI-90))
        final int lowestLogLevel = Math.min(consoleLevel.toInt(), fileLevel.toInt());
        LOG.setLevel(Level.toLevel(lowestLogLevel));

        if (!consoleLevel.equals(Level.OFF)) {
            // create and add console appender
            final PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
            consoleEncoder.setContext(loggerContext);
            consoleEncoder.setPattern(LOG_FORMAT_PATTERN);
            consoleEncoder.start();

            final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
            consoleAppender.setContext(loggerContext);
            consoleAppender.setName("STDOUT");
            consoleAppender.setEncoder(consoleEncoder);

            final ThresholdFilter consoleFilter = new ThresholdFilter();
            consoleFilter.setLevel(consoleLevel.levelStr);
            consoleFilter.setName(consoleLevel.levelStr);
            consoleFilter.start();
            consoleAppender.addFilter(consoleFilter);

            consoleAppender.start();
            LOG.addAppender(consoleAppender);
        }

        if (!fileLevel.equals(Level.OFF)) {
            // create file appender only if needed.
            // if done in the xml, it will create an empty file
            final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
            final FixedWindowRollingPolicy sizeBasedRollingPolicy = new FixedWindowRollingPolicy();
            final SizeBasedTriggeringPolicy<Object> sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy<>();

            fileAppender.setContext(loggerContext);
            sizeBasedTriggeringPolicy.setContext(loggerContext);
            sizeBasedRollingPolicy.setContext(loggerContext);
            fileAppender.setRollingPolicy(sizeBasedRollingPolicy);
            sizeBasedRollingPolicy.setParent(fileAppender);
            sizeBasedRollingPolicy.setMinIndex(0);
            sizeBasedRollingPolicy.setMaxIndex(99);

            final Path logFilePath = FileSystems.getDefault().getPath(LOG_DIR, LOG_FILE_NAME);
            fileAppender.setFile(logFilePath.toString());
            sizeBasedRollingPolicy.setFileNamePattern(LOG_DIR + LOG_ARCHIVE_FILE_PATTERN);
            sizeBasedRollingPolicy.start();

            sizeBasedTriggeringPolicy.setMaxFileSize("10MB");
            sizeBasedTriggeringPolicy.start();

            final PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
            fileEncoder.setContext(loggerContext);
            fileEncoder.setPattern(LOG_FORMAT_PATTERN);
            fileEncoder.start();

            fileAppender.setTriggeringPolicy((TriggeringPolicy)sizeBasedTriggeringPolicy);
            fileAppender.setRollingPolicy(sizeBasedRollingPolicy);
            fileAppender.setEncoder(fileEncoder);
            fileAppender.setName("LOGFILE");
            sizeBasedRollingPolicy.start();

            final ThresholdFilter fileFilter = new ThresholdFilter();
            fileFilter.setLevel(fileLevel.levelStr);
            fileFilter.setName(fileLevel.levelStr);
            fileFilter.start();
            fileAppender.addFilter(fileFilter);

            LOG.addAppender((Appender)fileAppender);
            fileAppender.start();
        }
    }

    public static void main(final String[] args) {
        try {
            final Properties props = CliUtils.readProperties(PROPERTY_FILE);

            // constructor parses for command, help, version, and logging settings
            final Arguments arguments = new Arguments(args);

            // turn root log wide open, filters will be set to argument levels
            configureLogging(arguments.getConsoleLogLevel(), arguments.getFileLogLevel());

            LOG.info("Version: {}", CliUtils.getVersion(props));
            LOG.info("Build Date: {}", CliUtils.getBuildDate(props));
            LOG.info("Command line args: {}", Joiner.on(", ").join(args));
            LOG.info("Console log level: {}", arguments.getConsoleLogLevel().toString());
            LOG.info("Log file log level: {}", arguments.getFileLogLevel().toString());
            LOG.info(CliCommand.getPlatformInformation());

            if(arguments.isHelp()) {
                printHelp(arguments);
                System.exit(0);
            }

            if (arguments.isPrintVersion()) {
                printVersion(props);
                System.exit(0);
            }

            // then it had better be a command
            try {
                if (arguments.getCommand() == null) {
                    throw new MissingOptionException(COMMAND.getOpt());
                }
            } catch (final IllegalArgumentException e) {
                throw new BadArgumentException("Unknown command", e);
            }

            final Ds3Client client = ClientFactory.createClient(arguments);
            if (!CliUtils.isVersionSupported(client)) {
                System.out.println(String.format("ERROR: Minimum Black Pearl supported is %s", CliUtils.MINIMUM_VERSION_SUPPORTED));
                System.exit(2);
            }

            final Ds3Provider provider = new Ds3ProviderImpl(client, Ds3ClientHelpers.wrap(client));
            final FileSystemProvider fileSystemProvider = new FileSystemProviderImpl();

            // get command, parse args
            final CliCommand command = CliCommandFactory.getCommandExecutor(arguments.getCommand()).withProvider(provider, fileSystemProvider);
            command.init(arguments);

            final CommandResponse response = command.render();
            System.out.println(response.getMessage());
            System.exit(response.getReturnCode());
        } catch (final Exception e) {
            EXCEPTION.handleException(e);
            System.exit(2);
        }
    }

    private static void printHelp(final Arguments arguments) throws CommandException, BadArgumentException {
        if(arguments.getHelp() == null) {
            // --help with no arg or -h prints basic usage
            arguments.printHelp();
            return;
        } else if (arguments.getHelp().equalsIgnoreCase("LIST_COMMANDS")) {
            System.out.println(CliCommandFactory.listAllCommands());
            return;
        }
        try {
            final CliCommand helpCommand = CliCommandFactory.getCommandExecutor(arguments.getHelp());
            // command help from Resource
            System.out.println(CliCommand.getVerboseHelp(arguments.getHelp()));
            // usage for command args
            helpCommand.printArgumentHelp(arguments);
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown command: " + arguments.getHelp() + "; use --help list_commands to get available commands.", e);
        }
    }

    private static void printVersion(final Properties props) {
        if (props == null) {
            System.err.println("Could not find property file.");
        } else {
            System.out.println("Version: " + CliUtils.getVersion(props));
            System.out.println("Build Date: " + CliUtils.getBuildDate(props));
        }
    }

}

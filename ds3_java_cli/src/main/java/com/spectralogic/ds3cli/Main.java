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
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.*;
import com.google.common.base.Joiner;
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.CliCommandFactory;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Main {
    // initialize and add appenders to root logger
    private final static ch.qos.logback.classic.Logger LOG =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final static String LOG_FORMAT_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} +++ %msg%n";
    private final static String LOG_ARCHIVE_FILE_PATTERN =  "spectra%i.log";
    private final static String LOG_DIR = "./ds3logs/";
    private final static String LOG_FILE_NAME = "spectra.log";


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

            final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
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
            final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
            final FixedWindowRollingPolicy sizeBasedRollingPolicy = new FixedWindowRollingPolicy();
            final SizeBasedTriggeringPolicy<Object> sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy<Object>();

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
            fileAppender.setEncoder((Encoder<ILoggingEvent>)fileEncoder);
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
        LOG.info("Console log level: {}", consoleLevel.toString());
        LOG.info("Log file log level: {}", fileLevel.toString());
    }

    public static void main(final String[] args) {

        try {
            // constructor parses for command, help, version, and logging settings
            final Arguments arguments = new Arguments(args);

            configureLogging(arguments.getConsoleLogLevel(), arguments.getFileLogLevel());
            LOG.info("Version: {}", arguments.getVersion());
            LOG.info("Command line args: {}", Joiner.on(", ").join(args));
            LOG.info(CliCommand.getPlatformInformation());
            LOG.info(arguments.getArgumentLog());

            // command help
            if (arguments.isHelp()) {
                if (arguments.getHelp().equalsIgnoreCase("LIST_COMMANDS")) {
                    System.out.println(CliCommandFactory.listAllCommands());
                } else {
                    System.out.println(CliCommand.getLongHelp(arguments.getHelp()));
                    CliCommand helpCommand = CliCommandFactory.getCommandExecutor(arguments.getHelp());
                    helpCommand.printArgumentHelp(arguments);
                }
                System.exit(0);
            }

            // get command, parse args
            CliCommand command = CliCommandFactory.getCommandExecutor(arguments.getCommand());
            command.init(arguments);

            final CommandResponse response = command.render();
            System.out.println(response.getMessage());
            System.exit(response.getReturnCode());
        } catch (final FailedRequestException e) {
            System.out.println("ERROR: " + e.getMessage());
            LOG.info("Stack trace: ", e);
            LOG.info("Printing out the response from the server:");
            LOG.info(((FailedRequestException) e).getResponseString());
            System.exit(2);
        } catch (final MissingOptionException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(2);
        } catch (final Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            LOG.info("Stack trace: ", e);
            System.exit(2);
        }
    }

}
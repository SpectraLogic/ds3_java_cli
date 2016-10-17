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
import ch.qos.logback.core.rolling.*;
import com.google.common.base.Joiner;
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.exceptions.CommandExceptionFactory;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.models.common.Credentials;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class Main {
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
            final Arguments arguments = new Arguments(args);

            // turn root log wide open, filters will be set to argument levels
            configureLogging(arguments.getConsoleLogLevel(), arguments.getFileLogLevel());

            LOG.info("Version: {}", arguments.getVersion());
            LOG.info("Command line args: {}", Joiner.on(", ").join(args));
            LOG.info("Console log level: {}", arguments.getConsoleLogLevel().toString());
            LOG.info("Log file log level: {}", arguments.getFileLogLevel().toString());
            LOG.info(CliCommand.getPlatformInformation());
            LOG.info(arguments.getArgumentLog());

            if (arguments.isHelp()) {
                // no need to connect to vend help
                final Ds3Cli runner = new Ds3Cli(null, arguments, null);
                final CommandResponse response = runner.getCommandHelp();
                System.out.println(response.getMessage());
                System.exit(response.getReturnCode());
            }

            final Ds3Client client = createClient(arguments);
            if (!Utils.isVersionSupported(client)) {
                System.out.println(String.format("ERROR: Minimum Black Pearl supported is %s", Utils.MINIMUM_VERSION_SUPPORTED));
                System.exit(2);
            }

            final Ds3Provider provider = new Ds3ProviderImpl(client, Ds3ClientHelpers.wrap(client));
            final FileUtils fileUtils = new FileUtilsImpl();

            final Ds3Cli runner = new Ds3Cli(provider, arguments, fileUtils);
            final CommandResponse response = runner.call();
            System.out.println(response.getMessage());
            System.exit(response.getReturnCode());
        } catch (final FailedRequestException e) {
            System.out.println(CommandExceptionFactory.getExcepionDescription(e));
            LOG.info("Stack trace: ", e);
            LOG.info("Printing out the response from the server:");
            LOG.info(e.getResponseString());
            System.exit(2);
        } catch (final Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            LOG.info("Stack trace: ", e);
            System.exit(2);
        }
    }

    public static Ds3Client createClient(final Arguments arguments) {
        final Ds3ClientBuilder builder = Ds3ClientBuilder.create(
                arguments.getEndpoint(),
                new Credentials(arguments.getAccessKey(), arguments.getSecretKey())
        )
                .withHttps(arguments.isHttps())
                .withCertificateVerification(arguments.isCertificateVerification())
                .withBufferSize(Integer.valueOf(arguments.getBufferSize()))
                .withRedirectRetries(arguments.getRetries());

        if (arguments.getProxy() != null) {
            builder.withProxy(arguments.getProxy());
        }
        return builder.build();
    }
}
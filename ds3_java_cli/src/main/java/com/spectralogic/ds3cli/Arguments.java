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
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.util.Metadata;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.WriteOptimization;
import com.spectralogic.ds3client.models.common.Credentials;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class Arguments {

    private final static String PROPERTY_FILE = "ds3_cli.properties";

    // variables required by Arguments class
    private final String[] args;
    private final Options options;
    private CommandLine cmd;
    private final List<String> missingArgs = new ArrayList<>();

    // locally stored properties processed in initial parsing
    // useful to provide defaults and handle parsing exceptions
    // most simple values are queried as needed
    private boolean help;
    private Level consoleLogLevel;
    private Level fileLogLevel;
    private ViewType outputFormat = ViewType.CLI;
    private String DEFAULT_RETRIES = "20";
    private String DEFAULT_BUFFERSIZE = "1048576";
    private String DEFAULT_NUMBEROFTHREADS = "10";
    private String version = "N/a";
    private String buildDate = "N/a";

    // don't use Logger because the user's preferences are not yet set
    // collect log info that will be logged by Main
    private static StringBuilder argumentLog = new StringBuilder("Argument processing");
    private void addToLog(final String logItem) { argumentLog.append(" | " + logItem) ; }
    public String getArgumentLog() { return argumentLog.toString(); }

    /**
     * add an argument option
     * @param opt Otion to add to Argumnets Options
     */
    public void addOption(Option opt) {
        this.options.addOption(opt);
    }
    /**
     * add an argument option and set its argument name
     * @param opt Otion to add to Argumnets Options
     * @param argName argumnet name in help display
     */
    public void addOption(Option opt, String argName) {
        opt.setArgName(argName);
        this.options.addOption(opt);
    }

    /**
     * retrieve an option value by option name
     * (Note: public static Option in Argumnets have convenience getters
     *  this would be used fro dynamically added options)
     * @param optionName Otion to query
     * @return a string representation of the option value
     */
    public String getOptionValue(final String optionName) {
        return (String)this.cmd.getOptionValue(optionName);
    }

    /**
     * retrieve multiple option values by option name
     * (Note: public static Option in Argumnets have convenience getters
     *  this would be used fro dynamically added options)
     * @param optionName Otion to query
     */
    public String[] getOptionValues(final String optionName) {
        final String[] values = this.getOptionValues(optionName);
        if ((values == null) || (values.length == 0)) {
            return null;
        }
        return values;
    }

    /**
     * see if an option has been set by option name
     * (Note: public static Option in Argumnets have convenience getters
     *  this would be used fro dynamically added options)
     * @param optionName Otion to query
     * @return true if option was set
     */
    public boolean optionExists(final String optionName) {
        if (this.cmd.hasOption(optionName)) {
            return true;
        }
        return false;
    }

    /**
     * retrieve an option value by option name or default value if not set
     * (Note: public static Option in Argumnets have convenience getters
     *  this would be used fro dynamically added options)
     * @param optionName Otion to query
     * @param defaultValue Object to return if not specified
     * @return option value as Object if secified on command, else defaultValue
     */
    public Object getOptionValueWithDefault(final String optionName, final Object defaultValue) {
        final Object returnValue = this.cmd.getOptionValue(optionName);
        if (returnValue != null) {
            return returnValue;
        }
        return defaultValue;
    }

    /**
     * instantiate object, set args array, complete initial parsing
     * @param args String array of command line tokens
     */
    public Arguments(final String[] args) throws BadArgumentException, ParseException {
        this.loadProperties();
        this.args = args;
        this.options = new Options();

        // parse all values required to select help, query version, or specify command, set up the client, and configure logging.
        addOption(endpoint, "endpoint");
        addOption(accessKey, "accessKeyId");
        addOption(secretKey, "secretKey");
        addOption(command, "command");
        addOption(proxy, "proxy");
        addOption(http, "http");
        addOption(insecure, "insecure");
        addOption(printHelp, "help");
        addOption(commandHelp, "commandHelp");
        addOption(verbose, "verbose");
        addOption(debug, "debug");
        addOption(trace, "trace");
        addOption(logVerbose, "log-verbose");
        addOption(logDebug, "log-debug");
        addOption(logTrace, "log-trace");
        addOption(printVersion, "version");
        addOption(viewType, "viewType");
        addOption(retries, "retries");
        addOption(bufferSize, "bufferSize");

        this.processCommandLine();
    }

    private String[] rootArgumentsOnly() throws BadArgumentException {
        // Present only command, help and version arguments to parser, After the command
        // is instantiated, the full parse will be run against the complete Options list.
        List<String> rootArguments = new ArrayList<String>();

        for (int i = 0; i < this.args.length; i++) {
            final String token = this.args[i];
            // allow get command (-c) and subsequent argument
            if (token.equals("-c")) {
                rootArguments.add(token);
                if (i < this.args.length) {
                    rootArguments.add(this.args[++i]);
                } else {
                    throw new BadArgumentException("No argument supplied for option: " + token);
                }
            }
            // allow get command help and subsequent argument (unless it is followed by an option (- or --)
            if (token.equals("--help")) {
                rootArguments.add(token);
                // might or might not have arg
                if ((i < this.args.length) && (!this.args[i + 1].startsWith("-"))) {
                    rootArguments.add(this.args[++i]);
                }
            }
            // allow version in root parse
            if ((token.equals("--version")) || (token.equals("-h"))) {
                rootArguments.add(token);
            }
            // allow logging level requests
            if ((token.equals("--trace")) || (token.equals("--debug")) || (token.equals("--verbose"))
                    || (token.equals("--log-trace")) || (token.equals("--log-debug")) || (token.equals("--logverbose"))) {
                rootArguments.add(token);
            }
        }

        // create a String[] of only valid root parse args.
        String[] ret = new String[rootArguments.size()];
        ret = rootArguments.toArray(ret);
        return ret;
    }

    /**
     * call after adding all options to reparse
     */
    public void parseCommandLine() throws ParseException, BadArgumentException  {
        parseCommandLine(false);
    }

    // parse command line. Set isRootTrue to first filter for only first parse options
    private void parseCommandLine(final boolean isRootParse) throws ParseException, BadArgumentException {
        final CommandLineParser parser = new DefaultParser();
        if (isRootParse) {
            this.cmd = parser.parse(this.options, rootArgumentsOnly());
        } else {
            this.cmd = parser.parse(this.options, this.args);
        }
    }

    // first parse
    private void processCommandLine() throws ParseException, BadArgumentException {
        parseCommandLine(true);

        // set the level for connsole and log file logging
        if (cmd.hasOption("trace")) {
            setConsoleLogLevel(Level.TRACE);
        } else if (cmd.hasOption("debug")) {
            setConsoleLogLevel(Level.DEBUG);
        } else if (cmd.hasOption("verbose")) {
            setConsoleLogLevel(Level.INFO);
        } else {
            setConsoleLogLevel(Level.OFF);
        }

        if (cmd.hasOption("log-trace")) {
            setFileLogLevel(Level.TRACE);
        } else if (cmd.hasOption("log-debug")) {
            setFileLogLevel(Level.DEBUG);
        } else if (cmd.hasOption("log-verbose")) {
            setFileLogLevel(Level.INFO);
        } else {
            setFileLogLevel(Level.OFF);
        }

        if (cmd.hasOption('h')) {
            this.printHelp();
            System.exit(0);
        }

        if (cmd.hasOption("help")) {
            setHelp(true);
            try {
                if (getHelp() == null) {
                    // if no arg on --help, print -h help
                    this.printHelp();
                    System.exit(0);
                }
                // no other options count
                return;
            } catch (final IllegalArgumentException e) {
                throw new BadArgumentException("Unknown command: " + getHelp() + "; use --help list_commands to get available commands.", e);
            }
        }

        if (cmd.hasOption("version")) {
            this.printVersion();
            System.exit(0);
        }

        if (cmd.hasOption("output-format")) {
            try {
                final String commandString = cmd.getOptionValue("output-format");
                this.setOutputFormat(ViewType.valueOf(commandString.toUpperCase()));
            } catch (final IllegalArgumentException e) {
                throw new BadArgumentException("Unknown command", e);
            }
        }

        try {
            if (getCommand() == null) {
                // must have -c or --help
                if (!isHelp()) {
                    throw new MissingOptionException("c");
                }
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown command", e);
        }
    }

    private void loadProperties() {
        final Properties props = new Properties();
        final InputStream input = Arguments.class.getClassLoader().getResourceAsStream(PROPERTY_FILE);
        if (input == null) {
            System.err.println("Could not find property file.");
        } else {
            try {
                props.load(input);
                this.version = (String) props.get("version");
                this.buildDate = (String) props.get("build.date");
            } catch (final IOException e) {
                System.err.println("Failed to load version property file.");
                if (this.getConsoleLogLevel() != Level.OFF) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printVersion() {
        System.out.println("Version: " + this.version);
        System.out.println("Build Date: " + this.buildDate);
    }

    public void printHelp() {
        // default help menu
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("ds3_java_cli", this.options);
    }

    // build client from Arguments
    public Ds3Client createClient() throws MissingOptionException {
        final Ds3ClientBuilder builder = Ds3ClientBuilder.create(
                getEndpoint(),
                new Credentials(getAccessKey(), getSecretKey())
        )
                .withHttps(isHttps())
                .withCertificateVerification(isCertificateVerification())
                .withBufferSize(Integer.valueOf(getBufferSize()))
                .withRedirectRetries(getRetries());

        if (getProxy() != null) {
            builder.withProxy(getProxy());
        }
        return builder.build();
    }

    private String getEndpoint() {
        String endpointValue =  (String)this.getOptionValue("e");
        if (Guard.isStringNullOrEmpty(endpointValue)) {
            endpointValue = System.getenv("DS3_ENDPOINT");
        }
        if (endpoint == null) {
            missingArgs.add("e");
            return "";
        }
        return endpointValue;
    }

    private String getAccessKey() {
        String accessKeyValue =  (String)this.getOptionValue("a");
        if (Guard.isStringNullOrEmpty(accessKeyValue)) {
            accessKeyValue = System.getenv("DS3_ACCESS_KEY");
            if (accessKeyValue == null) {
                missingArgs.add("a");
                return "";
            }
        }
        return accessKeyValue;
    }

    private String getSecretKey() {
        String secretKeyValue = (String) this.getOptionValue("k");
        if (Guard.isStringNullOrEmpty(secretKeyValue)) {
            secretKeyValue = System.getenv("DS3_SECRET_KEY");
            if (secretKeyValue == null) {
                missingArgs.add("k");
            }
        }
        return secretKeyValue;
    }

    private String getProxy() {
        // can be arg, environment var or null;
        String proxyValue = cmd.getOptionValue("x");
        if (Guard.isStringNullOrEmpty(proxyValue)) {
            proxyValue = System.getenv("http_proxy");
        }
        addToLog("Proxy: " + proxyValue);
        return proxyValue;
    }

    // conveninece getters for private options
    public String getVersion() {
        return this.version;
    }
    public String getBuildDate() {
        return this.buildDate;
    }
    public String getCommand() {
        return cmd.getOptionValue("c");
    }
    public String getHelp() {
        return cmd.getOptionValue("help");
    }
    public ViewType getOutputFormat() {
        return this.outputFormat;
    }
    void setOutputFormat(final ViewType outputFormat) {
        this.outputFormat = outputFormat;
    }
    public boolean isCertificateVerification() { return !this.optionExists("insecure"); }
    public boolean isHttps() {  return !this.optionExists("http"); }
    public boolean isHelp() { return this.help; }
    void setHelp(final boolean help) { this.help = help; }
    public Level getConsoleLogLevel() { return this.consoleLogLevel; }
    void setConsoleLogLevel(Level console) {this.consoleLogLevel = console; }
    public Level getFileLogLevel() { return this.fileLogLevel; }
    void setFileLogLevel(Level file) {this.fileLogLevel = file; }
    public int getRetries() {
        final String retryString = (String)this.getOptionValueWithDefault("r", DEFAULT_RETRIES);
        try {
            return Integer.parseInt(retryString);
        } catch (final NumberFormatException e) {
            System.err.printf("Error: Argument (%s) to '-r' was not a number\n", retryString);
            System.exit(1);
        }
        return -1;
    }

    // convenience getters for public options
    public String getBucket() { return this.getOptionValue("b"); }
    public String getDirectory() { return this.getOptionValue("d"); }
    public String getObjectName()  { return this.getOptionValue("o"); }
    public boolean isForce() { return this.optionExists("force"); }
    public String getPrefix()  { return this.getOptionValue("p"); }
    public boolean isChecksum() { return this.optionExists("checksum"); }
    public Priority getPriority() {
        final String priorityString = this.getOptionValue("priority");
        try {
            if (priorityString == null) {
                return null;
            } else {
                return Priority.valueOf(priorityString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            System.err.printf("Error: Unknown priority: %s", priorityString);
            System.exit(1);
        }
        return null;
    }
    public WriteOptimization getWriteOptimization() {
        final String writeOptimizationString = this.getOptionValue("writeOptimization");
        try {
            if (writeOptimizationString == null) {
                return null;
            } else {
                return WriteOptimization.valueOf(writeOptimizationString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            System.err.printf("Error: Unknown writeOptimization: %s", writeOptimizationString);
            System.exit(1);
        }
        return null;
    }
    public String getId() { return this.getOptionValue("-i"); }
    public boolean isCompleted() { return this.optionExists("completed"); }
    public boolean isSync() { return this.optionExists("sync"); }
    public String getNumberOfFiles() { return this.getOptionValue("n"); }
    public String getSizeOfFiles() { return this.getOptionValue("n"); }
    public String getBufferSize()  { return (String)this.getOptionValueWithDefault("bs", DEFAULT_BUFFERSIZE); }
    public String getNumberOfThreads()  { return (String)this.getOptionValueWithDefault("nt", DEFAULT_NUMBEROFTHREADS); }
    public boolean isIgnoreErrors() { return this.optionExists("ignore-errors"); }
    public boolean isFollowSymlinks()  {
        return (this.optionExists("follow-symlinks")  && !this.optionExists("no-follow-symlinks"));
    }
    public ImmutableMap<String, String> getMetadata() {
        final String[] meta = this.getOptionValues("metadata");
        if (meta == null) {
            return null;
        }
        return Metadata.parse(meta);
    }
    public ImmutableMap<String, String> getModifyParams() {
        final String[] meta = this.getOptionValues("modify-params");
        if (meta == null) {
            return null;
        }
        return Metadata.parse(meta);
    }
    public boolean isDiscard() { return this.optionExists("discard"); }

    public int getVerifyLastPercent() {
        return Integer.parseInt((String)getOptionValueWithDefault("verifyLastPercent", "20"));
    }

    public boolean doIgnoreNamingConflicts() { return this.optionExists("ignore-naming-conflicts"); }

    public boolean isInCache() { return this.optionExists("in-cache"); }


}




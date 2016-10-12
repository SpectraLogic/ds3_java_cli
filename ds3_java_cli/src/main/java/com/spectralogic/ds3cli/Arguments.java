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
    private String version = "N/a";
    private String buildDate = "N/a";
    private static final String DEFAULT_RETRIES = "20";
    private static final String DEFAULT_BUFFERSIZE = "1048576";
    private static final String DEFAULT_NUMBEROFTHREADS = "10";
    private static final String DEFAULT_VERIFY_PERCENT = "20";

    // don't use Logger because the user's preferences are not yet set
    // collect log info that will be logged by Main
    private static final StringBuilder argumentLog = new StringBuilder("Argument processing");
    private void addToLog(final String logItem) { argumentLog.append(" | ").append(logItem) ; }
    public String getArgumentLog() { return argumentLog.toString(); }

    /**
     * add an argument option
     * @param opt Option to add to Arguments Options
     */
    public void addOption(final Option opt) {
        this.options.addOption(opt);
    }
    /**
     * add an argument option and set its argument name
     * @param opt Option to add to Arguments Options
     * @param argName argument name in help display
     */
    public void addOption(final Option opt, final String argName) {
        opt.setArgName(argName);
        this.options.addOption(opt);
    }

    /**
     * retrieve an option value by option name
     * (Note: public static Option in Arguments have convenience getters
     *  this would be used for dynamically added options)
     * @param optionName Option to query
     * @return a string representation of the option value
     */
    public String getOptionValue(final String optionName) {
        final Object val = this.cmd.getOptionValue(optionName);
        if (val == null) {
            return null;
        }
        return val.toString();
    }

    /**
     * retrieve multiple option values by option name
     * (Note: public static Option in Arguments have convenience getters
     *  this would be used for dynamically added options)
     * @param optionName Option to query
     */
    public String[] getOptionValues(final String optionName) {
        final String[] values = this.cmd.getOptionValues(optionName);
        if (values == null || values.length == 0) {
            return null;
        }
        return values;
    }

    /**
     * see if an option has been set by option name
     * (Note: public static Option in Arguments have convenience getters
     *  this would be used for dynamically added options)
     * @param optionName Option to query
     * @return true if option was set
     */
    public boolean optionExists(final String optionName) {
        return this.cmd.hasOption(optionName);
    }

    /**
     * retrieve an option value by option name or default value if not set
     * (Note: public static Option in Arguments have convenience getters
     *  this would be used for dynamically added options)
     * @param optionName Option to query
     * @param defaultValue Object to return if not specified
     * @return option value as Object if secified on COMMAND, else defaultValue
     */
    public Object getOptionValueWithDefault(final String optionName, final Object defaultValue) {
        final Object returnValue = this.getOptionValue(optionName);
        if (returnValue != null) {
            return returnValue;
        }
        return defaultValue;
    }

    public static boolean matchesOption(final Option opt, final String token) {
        return token.equals('-' + opt.getOpt()) || token.equals("--" + opt.getLongOpt());
    }

    /**
     * instantiate object, set args array, complete initial parsing
     * @param args String array of COMMAND line tokens
     */
    public Arguments(final String[] args) throws BadArgumentException, ParseException {
        this.loadProperties();
        this.args = args;
        this.options = new Options();

        // parse all values required to select help, query version, or specify COMMAND, set up the client, and configure logging.
        addOption(ENDPOINT, "endpoint");
        addOption(ACCESS_KEY, "accessKeyId");
        addOption(SECRET_KEY, "secret_key");
        addOption(COMMAND, "command");
        addOption(PROXY, "proxy");
        addOption(HTTP, "http");
        addOption(INSECURE, "insecure");
        addOption(PRINT_HELP, "help");
        addOption(COMMAND_HELP, "command_help");
        addOption(VERBOSE, "verbose");
        addOption(DEBUG, "debug");
        addOption(TRACE, "trace");
        addOption(LOG_VERBOSE, "log-verbose");
        addOption(LOG_DEBUG, "log-debug");
        addOption(LOG_TRACE, "log-trace");
        addOption(PRINT_VERSION, "version");
        addOption(VIEW_TYPE, "view_type");
        addOption(RETRIES, "retries");
        addOption(BUFFER_SIZE, "buffer_size");

        this.processCommandLine();
    }

    private String[] rootArgumentsOnly() throws BadArgumentException {
        // Present only COMMAND, help and version arguments to parser, After the COMMAND
        // is instantiated, the full parse will be run against the complete Options list.
        final List<String> rootArguments = new ArrayList<String>();

        for (int i = 0; i < this.args.length; i++) {
            final String token = this.args[i];
            // allow get COMMAND (-c) and subsequent argument
            // or --output-format and subsequent argument
            if (matchesOption(COMMAND, token) || matchesOption(VIEW_TYPE,  token) ||
                    matchesOption(ACCESS_KEY, token) || matchesOption(SECRET_KEY,  token) ||
                    matchesOption(ENDPOINT, token) || matchesOption(PROXY,  token) ||
                    matchesOption(RETRIES, token)) {
                rootArguments.add(token);
                if (i < this.args.length) {
                    rootArguments.add(this.args[++i]);
                } else {
                    throw new BadArgumentException("No argument supplied for option: " + token);
                }
            }
            // allow get COMMAND help and subsequent argument (unless it is followed by an option (- or --)
            if (matchesOption(COMMAND_HELP, token)) {
                rootArguments.add(token);
                // might or might not have arg
                if ((i < this.args.length) && (!this.args[i + 1].startsWith("-"))) {
                    rootArguments.add(this.args[++i]);
                }
            }
            // allow version in root parse
            if ((matchesOption(VERBOSE, token)) || (matchesOption(PRINT_HELP, token))
                    || (matchesOption(TRACE, token)) || (matchesOption(DEBUG, token)) || (matchesOption(VERBOSE, token))
                    || (matchesOption(LOG_TRACE, token)) || (matchesOption(LOG_DEBUG, token))
                    || (matchesOption(LOG_VERBOSE, token))) {
                rootArguments.add(token);
            }
        }

        // create a String[] of only valid root parse args.
        return rootArguments.toArray(new String[rootArguments.size()]);
    }

    /**
     * call after adding all options to reparse
     */
    public void parseCommandLine() throws ParseException, BadArgumentException  {
        parseCommandLine(false);
    }

    // parse COMMAND line. Set isRootTrue to first filter for only first parse options
    private void parseCommandLine(final boolean isRootParse) throws ParseException, BadArgumentException {
        final CommandLineParser parser = new DefaultParser();
        if (isRootParse) {
            final String[] roots =  rootArgumentsOnly();
            this.cmd = parser.parse(this.options, roots);
        } else {
            this.cmd = parser.parse(this.options, this.args);
        }
    }

    // first parse
    private void processCommandLine() throws ParseException, BadArgumentException {
        parseCommandLine(true);

        // set the level for connsole and log file logging
        if (cmd.hasOption(TRACE.getLongOpt())) {
            setConsoleLogLevel(Level.TRACE);
        } else if (cmd.hasOption(DEBUG.getLongOpt())) {
            setConsoleLogLevel(Level.DEBUG);
        } else if (cmd.hasOption(VERBOSE.getLongOpt())) {
            setConsoleLogLevel(Level.INFO);
        } else {
            setConsoleLogLevel(Level.OFF);
        }

        if (cmd.hasOption(LOG_TRACE.getLongOpt())) {
            setFileLogLevel(Level.TRACE);
        } else if (cmd.hasOption(LOG_DEBUG.getLongOpt())) {
            setFileLogLevel(Level.DEBUG);
        } else if (cmd.hasOption(LOG_VERBOSE.getLongOpt())) {
            setFileLogLevel(Level.INFO);
        } else {
            setFileLogLevel(Level.OFF);
        }

        if (cmd.hasOption(PRINT_HELP.getOpt())) {
            this.printHelp();
            System.exit(0);
        }

        if (cmd.hasOption(COMMAND_HELP.getLongOpt())) {
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

        if (cmd.hasOption(PRINT_VERSION.getLongOpt())) {
            this.printVersion();
            System.exit(0);
        }

        try {
            if (getCommand() == null) {
                // must have -c or --help
                if (!isHelp()) {
                    throw new MissingOptionException(COMMAND.getOpt());
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
                this.version = props.get("version").toString();
                this.buildDate = props.get("build.date").toString();
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
    public Ds3Client createClient() throws MissingOptionException, BadArgumentException {
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
        String endpointValue =  this.getOptionValue(ENDPOINT.getOpt());
        if (Guard.isStringNullOrEmpty(endpointValue)) {
            endpointValue = System.getenv("DS3_ENDPOINT");
        }
        if (ENDPOINT == null) {
            missingArgs.add(ENDPOINT.getOpt());
            return "";
        }
        return endpointValue;
    }

    private String getAccessKey() {
        String accessKeyValue =  this.getOptionValue(ACCESS_KEY.getOpt());
        if (Guard.isStringNullOrEmpty(accessKeyValue)) {
            accessKeyValue = System.getenv("DS3_ACCESS_KEY");
            if (accessKeyValue == null) {
                missingArgs.add(ACCESS_KEY.getOpt());
                return "";
            }
        }
        return accessKeyValue;
    }

    private String getSecretKey() {
        String secretKeyValue = this.getOptionValue(SECRET_KEY.getOpt());
        if (Guard.isStringNullOrEmpty(secretKeyValue)) {
            secretKeyValue = System.getenv("DS3_SECRET_KEY");
            if (secretKeyValue == null) {
                missingArgs.add(SECRET_KEY.getOpt());
            }
        }
        return secretKeyValue;
    }

    private String getProxy() {
        // can be arg, environment var or null;
        String proxyValue = cmd.getOptionValue(PROXY.getOpt());
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
        return cmd.getOptionValue(COMMAND.getOpt());
    }
    public String getHelp() {
        return cmd.getOptionValue(COMMAND_HELP.getLongOpt());
    }
    public ViewType getOutputFormat() {
        return ViewType.valueOf(this.getOptionValueWithDefault(VIEW_TYPE.getLongOpt(), ViewType.CLI).toString().toUpperCase());
    }

    public boolean isCertificateVerification() { return !this.optionExists(INSECURE.getLongOpt()); }
    public boolean isHttps() {  return !this.optionExists(HTTP.getLongOpt()); }
    public boolean isHelp() { return this.help; }
    void setHelp(final boolean help) { this.help = help; }
    public Level getConsoleLogLevel() { return this.consoleLogLevel; }
    void setConsoleLogLevel(final Level console) {this.consoleLogLevel = console; }
    public Level getFileLogLevel() { return this.fileLogLevel; }
    void setFileLogLevel(final Level file) {this.fileLogLevel = file; }

    public int getRetries() throws BadArgumentException {
        final String retryString = this.getOptionValueWithDefault(RETRIES.getOpt(), DEFAULT_RETRIES).toString();
        try{
            return Integer.parseInt(retryString);
        } catch (NumberFormatException e) {
            throw new BadArgumentException("Argument to '-r' was not a number", e);
        }
    }

    // convenience getters for public options
    public String getBucket() { return this.getOptionValue(BUCKET.getOpt()); }
    public String getDirectory() { return this.getOptionValue(DIRECTORY.getOpt()); }
    public String getObjectName()  { return this.getOptionValue(OBJECT_NAME.getOpt()); }
    public boolean isForce() { return this.optionExists(FORCE.getLongOpt()); }
    public String getPrefix()  { return this.getOptionValueWithDefault(PREFIX.getOpt(), "").toString(); }
    public boolean isChecksum() { return this.optionExists(CHECKSUM.getLongOpt()); }

    public Priority getPriority() throws BadArgumentException {
        final String priorityString = this.getOptionValue(PRIORITY.getLongOpt());
        try {
            if (priorityString == null) {
                return null;
            } else {
                return Priority.valueOf(priorityString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown priority: " + priorityString, e);
        }
    }

    public WriteOptimization getWriteOptimization() throws BadArgumentException{
        final String writeOptimizationString = this.getOptionValue(WRITE_OPTIMIZATION.getLongOpt());
        try {
            if (writeOptimizationString == null) {
                return null;
            } else {
                return WriteOptimization.valueOf(writeOptimizationString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Error: Unknown write_optimization:" + writeOptimizationString, e);
        }
    }

    public String getId() { return this.getOptionValue(ID.getOpt()); }
    public boolean isCompleted() { return this.optionExists(COMPLETED.getLongOpt()); }
    public boolean isSync() { return this.optionExists(SYNC.getLongOpt()); }
    public String getNumberOfFiles() { return this.getOptionValue(NUMBER_OF_FILES.getOpt()); }
    public String getSizeOfFiles() { return this.getOptionValue(SIZE_OF_FILES.getOpt()); }
    public String getBufferSize()  { return this.getOptionValueWithDefault(BUFFER_SIZE.getOpt(), DEFAULT_BUFFERSIZE).toString(); }
    public String getNumberOfThreads()  { return this.getOptionValueWithDefault(NUMBER_OF_THREADS.getOpt(), DEFAULT_NUMBEROFTHREADS).toString(); }
    public boolean isIgnoreErrors() { return this.optionExists(IGNORE_ERRORS.getLongOpt()); }
    public boolean isFollowSymlinks()  {
        return (this.optionExists(FOLLOW_SYMLINKS.getLongOpt())  && !this.optionExists(NO_FOLLOW_SYMLINKS.getLongOpt()));
    }
    public ImmutableMap<String, String> getMetadata() {
        final String[] meta = this.getOptionValues(METADATA.getLongOpt());
        if (meta == null) {
            return null;
        }
        return Metadata.parse(meta);
    }
    public ImmutableMap<String, String> getModifyParams() {
        final String[] meta = this.getOptionValues(MODIFY_PARAMS.getLongOpt());
        if (meta == null) {
            return null;
        }
        return Metadata.parse(meta);
    }
    public ImmutableMap<String, String> getFilterParams() {
        final String[] meta = this.getOptionValues(FILTER_PARAMS.getLongOpt());
        if (meta == null) {
            return null;
        }
        return Metadata.parse(meta);
    }

    public boolean isDiscard() { return this.optionExists(DISCARD.getLongOpt()); }

    public int getVerifyLastPercent() {
        return Integer.parseInt(getOptionValueWithDefault(VERIFY_PERCENT.getLongOpt(), DEFAULT_VERIFY_PERCENT).toString());
    }

    public boolean doIgnoreNamingConflicts() { return this.optionExists(IGNORE_NAMING_CONFLICTS.getLongOpt()); }

    public boolean isInCache() { return this.optionExists(IN_CACHE.getLongOpt()); }

}




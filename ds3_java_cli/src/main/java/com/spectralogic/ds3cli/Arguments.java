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
    private ViewType outputFormat = ViewType.CLI;
    private String version = "N/a";
    private String buildDate = "N/a";
    private static final String DEFAULT_RETRIES = "20";
    private static final String DEFAULT_BUFFERSIZE = "1048576";
    private static final String DEFAULT_NUMBEROFTHREADS = "10";

    // don't use Logger because the user's preferences are not yet set
    // collect log info that will be logged by Main
    private static StringBuilder argumentLog = new StringBuilder("Argument processing");
    private void addToLog(final String logItem) { argumentLog.append(" | " ).append(logItem) ; }
    public String getArgumentLog() { return argumentLog.toString(); }

    // These options are added in the ARguments class and read in first parsing
    private final static Option ENDPOINT = new Option("e", true, "The ds3 ENDPOINT to connect to or have \"DS3_ENDPOINT\" set as an environment variable.");
    private final static Option ACCESS_KEY = new Option("a", true, "Access Key ID or have \"DS3_ACCESS_KEY\" set as an environment variable");
    private final static Option SECRET_KEY = new Option("k", true, "Secret access key or have \"DS3_SECRET_KEY\" set as an environment variable");
    private final static Option COMMAND = new Option("c", true, "The Command to execute.  For Possible values, use '--help list_commands.'" );
    private final static Option PROXY = new Option("x", true, "The URL of the PROXY server to use or have \"http_proxy\" set as an environment variable");
    private final static Option HTTP = Option.builder().longOpt("HTTP").desc("Send all requests over standard HTTP").build();
    private final static Option INSECURE = Option.builder().longOpt("INSECURE").desc("Ignore ssl certificate verification").build();
    private final static Option PRINT_HELP = new Option("h", "Help Menu");
    private final static Option COMMAND_HELP = Option.builder()
            .longOpt("help")
            .desc("Command Help (provide COMMAND name from -c)")
            .optionalArg(true)
            .numberOfArgs(1)
            .build();
    private static final Option VERBOSE = Option.builder().longOpt("verbose").desc("Log output to console.").build();
    private static final Option DEBUG = Option.builder().longOpt("debug").desc("Debug (more verbose) output to console.").build();
    private static final Option TRACE = Option.builder().longOpt("trace").desc("Trace (most verbose) output to console.").build();
    private static final Option LOG_VERBOSE = Option.builder().longOpt("log-verbose").desc("Log output to log file.").build();
    private static final Option LOG_DEBUG = Option.builder().longOpt("log-debug").desc("Debug (more verbose) output to log file.").build();
    private static final Option LOG_TRACE = Option.builder().longOpt("log-trsce").desc("Trace (most verbose) output to log file.").build();
    private static final Option PRINT_VERSION = Option.builder().longOpt("version").desc("Print version information").build();
    private static final Option VIEW_TYPE = Option.builder().longOpt("output-format")
            .desc("Configure how the output should be displayed.  Possible values: [" + ViewType.valuesString() + "]").build();
    private static final Option RETRIES = new Option("r", true,
            "Specifies how many times puts and gets will be attempted before failing the request. The default is 5");
    private static final Option BUFFER_SIZE = new Option("bs", true, "Set the buffer size in bytes. The defalut is 1MB");

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
        return this.cmd.getOptionValue(optionName).toString();
    }

    /**
     * retrieve multiple option values by option name
     * (Note: public static Option in Arguments have convenience getters
     *  this would be used for dynamically added options)
     * @param optionName Option to query
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
     * (Note: public static Option in Arguments have convenience getters
     *  this would be used for dynamically added options)
     * @param optionName Option to query
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
     * (Note: public static Option in Arguments have convenience getters
     *  this would be used for dynamically added options)
     * @param optionName Option to query
     * @param defaultValue Object to return if not specified
     * @return option value as Object if secified on COMMAND, else defaultValue
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
        List<String> rootArguments = new ArrayList<String>();

        for (int i = 0; i < this.args.length; i++) {
            final String token = this.args[i];
            // allow get COMMAND (-c) and subsequent argument
            if (token.equals("-c")) {
                rootArguments.add(token);
                if (i < this.args.length) {
                    rootArguments.add(this.args[++i]);
                } else {
                    throw new BadArgumentException("No argument supplied for option: " + token);
                }
            }
            // allow get COMMAND help and subsequent argument (unless it is followed by an option (- or --)
            if (token.equals("--help")) {
                rootArguments.add(token);
                // might or might not have arg
                if ((i < this.args.length) && (!this.args[i + 1].startsWith("-"))) {
                    rootArguments.add(this.args[++i]);
                }
            }
            // allow version in root parse
            if ((token.equals("--version")) || (token.equals("-h"))
                    || (token.equals("--trace")) || (token.equals("--debug")) || (token.equals("--verbose"))
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

    // parse COMMAND line. Set isRootTrue to first filter for only first parse options
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
        String endpointValue =  this.getOptionValue("e");
        if (Guard.isStringNullOrEmpty(endpointValue)) {
            endpointValue = System.getenv("DS3_ENDPOINT");
        }
        if (ENDPOINT == null) {
            missingArgs.add("e");
            return "";
        }
        return endpointValue;
    }

    private String getAccessKey() {
        String accessKeyValue =  this.getOptionValue("a");
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
        String secretKeyValue = this.getOptionValue("k");
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
        final String retryString = this.getOptionValueWithDefault("r", DEFAULT_RETRIES).toString();
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
        final String writeOptimizationString = this.getOptionValue("write_optimization");
        try {
            if (writeOptimizationString == null) {
                return null;
            } else {
                return WriteOptimization.valueOf(writeOptimizationString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            System.err.printf("Error: Unknown write_optimization: %s", writeOptimizationString);
            System.exit(1);
        }
        return null;
    }
    public String getId() { return this.getOptionValue("-i"); }
    public boolean isCompleted() { return this.optionExists("completed"); }
    public boolean isSync() { return this.optionExists("sync"); }
    public String getNumberOfFiles() { return this.getOptionValue("n"); }
    public String getSizeOfFiles() { return this.getOptionValue("n"); }
    public String getBufferSize()  { return this.getOptionValueWithDefault("bs", DEFAULT_BUFFERSIZE).toString(); }
    public String getNumberOfThreads()  { return this.getOptionValueWithDefault("nt", DEFAULT_NUMBEROFTHREADS).toString(); }
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
        return Integer.parseInt(getOptionValueWithDefault("verifyLastPercent", "20").toString());
    }

    public boolean doIgnoreNamingConflicts() { return this.optionExists("ignore-naming-conflicts"); }

    public boolean isInCache() { return this.optionExists("in-cache"); }


}




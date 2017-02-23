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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.util.MetadataUtils;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.WriteOptimization;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class Arguments {

    // Root args are added before first parse. All are optional
    private final static ImmutableList<Option> rootArgs
            = ImmutableList.of(ENDPOINT, ACCESS_KEY, SECRET_KEY, COMMAND, PROXY, HTTP, INSECURE, PRINT_HELP, COMMAND_HELP,
            VERBOSE, DEBUG, TRACE, LOG_VERBOSE, LOG_DEBUG, LOG_TRACE, PRINT_VERSION, VIEW_TYPE, RETRIES, BUFFER_SIZE);

    private final String[] args;
    private final Options options;
    private CommandLine cmd;

    // locally stored properties processed in initial parsing
    // useful to provide defaults and handle parsing exceptions
    // most simple values are queried as needed
    private Level consoleLogLevel;
    private Level fileLogLevel;
    private static final String DEFAULT_VERIFY_PERCENT = "20";
    private static final int DEFAULT_RETRIES = 20;
    private static final int DEFAULT_BUFFERSIZE = 1024 * 1024;
    private static final int DEFAULT_NUMBEROFTHREADS = 10;

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
     * @return option value as Object if specified on COMMAND, else defaultValue
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
    public Arguments(final String[] args) throws ParseException {
        this.args = args;
        this.options = new Options();

        // parse all values required to select help, query version, or specify COMMAND, set up the client, and configure logging.
        addRootArguments();
        this.processCommandLine();
    }

    void addRootArguments() {
        for (final Option optionRoot : rootArgs) {
            optionRoot.setRequired(false);
            addOption(optionRoot, optionRoot.getArgName());
        }
    }

    private String[] filterRootArguments() {
        // Returns only arguments in rootArguments
        // After the command is instantiated, the full parse will be run against the complete Options list.
        final List<String> rootArguments = new ArrayList<>();

        for (int i = 0; i < this.args.length; i++) {
            final String token = this.args[i];

            // special case: --help can have an arg or not
            if (matchesOption(COMMAND_HELP, token)) {
                rootArguments.add(token);
                // might or might not have arg
                if (i + 1 < this.args.length && !this.args[i + 1].startsWith("-")) {
                    rootArguments.add(this.args[++i]);
                }
                break;
            }

            for (final Option rootArg : rootArgs) {
                if (matchesOption(rootArg, token)) {
                    // add the option token
                    rootArguments.add(token);
                    if (rootArg.hasArg()) {
                        // add its argument
                        rootArguments.add(this.args[++i]);
                    }
                }
            }
        }
        // create a String[] of only valid root parse args.
        return rootArguments.toArray(new String[rootArguments.size()]);
    }

    /**
     * call after adding all options to reparse
     */
    public void parseCommandLine() throws ParseException {
        parseAllCommands();
    }

    // parse command line. Set isRootParse true to first filter for only first parse options
    private void parseRootCommands() throws ParseException {
        final CommandLineParser parser = new DefaultParser();
            final String[] roots =  filterRootArguments();
            this.cmd = parser.parse(this.options, roots);
    }

    // parse command line. Set isRootParse true to first filter for only first parse options
    private void parseAllCommands() throws ParseException {
        final CommandLineParser parser = new DefaultParser();
        this.cmd = parser.parse(this.options, this.args);
    }

    // initial parse of root commands
    private void processCommandLine() throws ParseException {
        parseRootCommands();

        // set the level for console and log file logging
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
    }

    public void printHelp() {
        // default help menu
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("ds3_java_cli", this.options);
    }

    private String getEndpoint() {
        return this.getOptionValue(ENDPOINT.getOpt());
    }

    private String getAccessKey() {
        return this.getOptionValue(ACCESS_KEY.getOpt());
    }

    private String getSecretKey() {
        return this.getOptionValue(SECRET_KEY.getOpt());
    }

    private String getProxy() {
        return this.getOptionValue(PROXY.getOpt());
    }

    // convenience getters for private options
    public boolean isPrintVersion() { return this.optionExists(PRINT_VERSION.getLongOpt()); }
    public String getCommand() {
        return cmd.getOptionValue(COMMAND.getOpt());
    }
    public String getHelp() {
        return this.getOptionValue(COMMAND_HELP.getLongOpt()) ;
    }
    public ViewType getOutputFormat() {
        return ViewType.valueOf(this.getOptionValueWithDefault(VIEW_TYPE.getLongOpt(), ViewType.CLI).toString().toUpperCase());
    }
    public boolean isHelp() {
        return this.optionExists(COMMAND_HELP.getLongOpt()) || this.optionExists(PRINT_HELP.getOpt());
    }

    public boolean isCertificateVerification() {
        return !this.optionExists(INSECURE.getLongOpt());
    }

    public boolean isHttps() {
        return !this.optionExists(HTTP.getLongOpt());
    }

    public int getRetries() throws BadArgumentException {
        final String retryString = this.getOptionValueWithDefault(RETRIES.getOpt(), DEFAULT_RETRIES).toString();
        try {
            return Integer.parseInt(retryString);
        } catch (final NumberFormatException e) {
            throw new BadArgumentException("Argument to " +  RETRIES.getOpt() + "was not a number", e);
        }
    }

    public int getNumberOfThreads()  throws BadArgumentException {
        final String threadsString = this.getOptionValueWithDefault(NUMBER_OF_THREADS.getOpt(), DEFAULT_NUMBEROFTHREADS).toString();
        try {
            return Integer.parseInt(threadsString);
        } catch (final NumberFormatException e) {
            throw new BadArgumentException("Argument to " + NUMBER_OF_THREADS.getOpt() + "was not a number", e);
        }
    }

    public int getBufferSize()  throws BadArgumentException {
        final String bufferSizeString = this.getOptionValueWithDefault(BUFFER_SIZE.getOpt(), DEFAULT_BUFFERSIZE).toString();
        try {
            return Integer.parseInt(bufferSizeString);
        } catch (final NumberFormatException e) {
            throw new BadArgumentException("Argument to " + BUFFER_SIZE.getOpt() + "was not a number", e);
        }
    }

    public Level getConsoleLogLevel() { return this.consoleLogLevel; }
    void setConsoleLogLevel(final Level console) {this.consoleLogLevel = console; }
    public Level getFileLogLevel() { return this.fileLogLevel; }
    void setFileLogLevel(final Level file) {this.fileLogLevel = file; }


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
    public boolean isIgnoreErrors() { return this.optionExists(IGNORE_ERRORS.getLongOpt()); }
    public boolean isFollowSymlinks()  {
        return this.optionExists(FOLLOW_SYMLINKS.getLongOpt())  && !this.optionExists(NO_FOLLOW_SYMLINKS.getLongOpt());
    }
    public ImmutableMap<String, String> getMetadata() {
        final String[] meta = this.getOptionValues(METADATA.getLongOpt());
        if (meta == null) {
            return null;
        }
        return MetadataUtils.parse(meta);
    }
    public ImmutableMap<String, String> getModifyParams() {
        final String[] meta = this.getOptionValues(MODIFY_PARAMS.getLongOpt());
        if (meta == null) {
            return null;
        }
        return MetadataUtils.parse(meta);
    }
    public ImmutableMap<String, String> getFilterParams() {
        final String[] meta = this.getOptionValues(FILTER_PARAMS.getLongOpt());
        if (meta == null) {
            return null;
        }
        return MetadataUtils.parse(meta);
    }

    public boolean isDiscard() { return this.optionExists(DISCARD.getLongOpt()); }

    public int getVerifyLastPercent() {
        return Integer.parseInt(getOptionValueWithDefault(VERIFY_PERCENT.getLongOpt(), DEFAULT_VERIFY_PERCENT).toString());
    }

    public boolean doIgnoreNamingConflicts() { return this.optionExists(IGNORE_NAMING_CONFLICTS.getLongOpt()); }

    public boolean isInCache() { return this.optionExists(IN_CACHE.getLongOpt()); }

    public String GetEjectLabel() {return this.getOptionValue(EJECT_LABEL.getLongOpt()); }

    public String GetEjectLocation() {return this.getOptionValue(EJECT_LOCATION.getLongOpt()); }

    public String getRangeOffset() {return this.getOptionValue(RANGE_OFFSET.getLongOpt()); }

    public String getRangeLength() {return this.getOptionValue(RANGE_LENGTH.getLongOpt()); }

    public String[] getAllArgs() {return this.args; }

}




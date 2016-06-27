/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.util.Metadata;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.WriteOptimization;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Arguments {

    private final static Logger LOG = LoggerFactory.getLogger(Arguments.class);
    private final static String PROPERTY_FILE = "ds3_cli.properties";

    private final Options options;

    private String bucket;
    private String directory;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String prefix;
    private String id = null;
    private CommandValue command;
    private boolean help;
    private final String[] args;
    private String objectName;
    private String proxy;
    private int retries = 20;
    private Priority priority;
    private WriteOptimization writeOptimization;
    private boolean force = false;
    private boolean checksum = false;
    private boolean certificateVerification = true;
    private boolean https = true;
    private boolean completed = false;
    private boolean sync = false;
    private String numberOfFiles;
    private String sizeOfFiles;
    private boolean discard = false;
    private ViewType outputFormat = ViewType.CLI;

    private String version = "N/a";
    private String buildDate = "N/a";
    private String bufferSize;
    private String numberOfThreads;
    private boolean ignoreErrors = false;
    private boolean followSymlinks = false;
    private ImmutableMap<String, String> metadata = null;
    private ImmutableMap<String, String> modifyParams = null;

    public Arguments(final String[] args) throws BadArgumentException, ParseException {
        this.loadProperties();
        this.args = args;
        this.options = new Options();

        final Option ds3Endpoint = new Option("e", true, "The ds3 endpoint to connect to or have \"DS3_ENDPOINT\" set as an environment variable.");
        ds3Endpoint.setArgName("endpoint");
        final Option bucket = new Option("b", true, "The ds3 bucket to copy to");
        bucket.setArgName("bucket");
        final Option accessKey = new Option("a", true, "Access Key ID or have \"DS3_ACCESS_KEY\" set as an environment variable");
        accessKey.setArgName("accessKeyId");
        final Option secretKey = new Option("k", true, "Secret access key or have \"DS3_SECRET_KEY\" set as an environment variable");
        secretKey.setArgName("secretKey");
        final Option command = new Option("c", true, "The Command to execute.  Possible values: [" + CommandValue.valuesString() + "] Use '--help <command>' for more information " );
        command.setArgName("command");
        final Option directory = new Option("d", true, "Specify a directory to interact with if required");
        directory.setArgName("directory");
        final Option objectName = new Option("o", true, "The name of the object to be retrieved or stored");
        objectName.setArgName("objectFileName");
        final Option prefix = new Option("p", true, "Used with 'get_bulk' to restore only objects who's names start with prefix.  Also used with 'bulk_put' and 'put_object' to add a prefix to object name(s)");
        prefix.setArgName("prefix");
        final Option proxy = new Option("x", true, "The URL of the proxy server to use or have \"http_proxy\" set as an environment variable");
        proxy.setArgName("proxy");
        final Option id = new Option("i", true, "ID for identifying ds3 api resources");
        id.setArgName("id");
        final Option force = new Option(null, false, "Used to force an operation even if there is an error");
        force.setLongOpt("force");
        final Option retries = new Option("r", true, "Specifies how many times puts and gets will be attempted before failing the request.  The default is 5");
        retries.setArgName("retries");
        final Option checksum = new Option(null, "Validate checksum values");
        checksum.setLongOpt("checksum");
        final Option priority = new Option(null, true, "Set the bulk job priority.  Possible values: [" + Priority.values() + "]");
        priority.setLongOpt("priority");
        priority.setArgName("priority");
        final Option writeOptimization = new Option(null, true, "Set the job write optimization.  Possible values: [" + WriteOptimization.values() + "]");
        writeOptimization.setLongOpt("writeOptimization");
        writeOptimization.setArgName("writeOptimization");
        final Option help = new Option("h", "Help Menu");
        final Option commandHelp = new Option(null, true, "Command Help (provide command name from -c)");
        commandHelp.setLongOpt("help");
        final Option http = new Option(null, "Send all requests over standard http");
        http.setLongOpt("http");
        final Option insecure = new Option(null, "Ignore ssl certificate verification");
        insecure.setLongOpt("insecure");
        final Option version = new Option(null, "Print version information");
        version.setLongOpt("version");
        final Option verbose = new Option(null, "Verbose output");
        verbose.setLongOpt("verbose");
        final Option debug = new Option(null, "Debug output.  If set takes precedence over the 'verbose' option");
        debug.setLongOpt("debug");
        final Option trace = new Option(null, "Trace output");
        trace.setLongOpt("trace");
        final Option viewType = new Option(null, true, "Configure how the output should be displayed.  Possible values: [" + ViewType.valuesString() + "]");
        viewType.setLongOpt("output-format");
        final Option completed = new Option(null, false, "Used with the command get_jobs to include the display of completed jobs");
        completed.setLongOpt("completed");
        final Option sync = new Option(null, false, "Copy only the newest files");
        sync.setLongOpt("sync");
        final Option numberOfFiles = new Option("n", true, "The number of files for the performance test");
        numberOfFiles.setArgName("numberOfFiles");
        final Option sizeOfFiles = new Option("s", true, "The size (in MB) for each file in the performance test");
        sizeOfFiles.setArgName("sizeOfFiles");
        final Option bufferSize = new Option("bs", true, "Set the buffer size in bytes");
        bufferSize.setArgName("bufferSize");
        final Option numberOfThreads = new Option("nt", true, "Set the number of threads");
        numberOfThreads.setArgName("numberOfThreads");
        final Option ignoreErrors = new Option(null, false, "Ignore files that cause errors");
        ignoreErrors.setLongOpt("ignore-errors");
        final Option noFollowSymlinks = new Option(null, false, "Set to not follow symlinks, this is the default behavior");
        noFollowSymlinks.setLongOpt("no-follow-symlinks");
        final Option followSymLinks = new Option(null, false, "Set to follow symlinks");
        followSymLinks.setLongOpt("follow-symlinks");
        final Option metadata = OptionBuilder.withLongOpt("metadata")
                .withDescription("Metadata for when putting a single object.  Using the format: key:value,key2:value2")
                .hasArgs()
                .withValueSeparator(',')
                .create();
        final Option modifyParams = OptionBuilder.withLongOpt("modify-params")
                .withDescription("Parameters for modifying features using the format key:value,key2:value2. For modify_user: default_data_policy_id")
                .hasArgs()
                .withValueSeparator(',')
                .create();
        final Option discard = new Option(null, false, "Discard restoration data (/dev/null) in get_bulk");
        discard.setLongOpt("discard");

        this.options.addOption(ds3Endpoint);
        this.options.addOption(bucket);
        this.options.addOption(directory);
        this.options.addOption(accessKey);
        this.options.addOption(secretKey);
        this.options.addOption(command);
        this.options.addOption(objectName);
        this.options.addOption(prefix);
        this.options.addOption(proxy);
        this.options.addOption(id);
        this.options.addOption(force);
        this.options.addOption(retries);
        this.options.addOption(checksum);
        this.options.addOption(priority);
        this.options.addOption(writeOptimization);
        this.options.addOption(help);
        this.options.addOption(commandHelp);
        this.options.addOption(insecure);
        this.options.addOption(http);
        this.options.addOption(version);
        this.options.addOption(verbose);
        this.options.addOption(debug);
        this.options.addOption(trace);
        this.options.addOption(viewType);
        this.options.addOption(completed);
        this.options.addOption(sync);
        this.options.addOption(numberOfFiles);
        this.options.addOption(sizeOfFiles);
        this.options.addOption(bufferSize);
        this.options.addOption(numberOfThreads);
        this.options.addOption(ignoreErrors);
        this.options.addOption(noFollowSymlinks);
        this.options.addOption(followSymLinks);
        this.options.addOption(metadata);
        this.options.addOption(modifyParams);
        this.options.addOption(discard);
        this.processCommandLine();
    }

    private void processCommandLine() throws ParseException, BadArgumentException {
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(this.options, this.args);

        final List<String> missingArgs = new ArrayList<>();

        final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        if (cmd.hasOption("trace")) {
            rootLogger.setLevel(Level.TRACE);
            rootLogger.trace("Trace output enabled");
        } else if (cmd.hasOption("debug")) {
            rootLogger.setLevel(Level.DEBUG);
            rootLogger.debug("Debug output enabled");
        } else if (cmd.hasOption("verbose")) {
            rootLogger.setLevel(Level.INFO);
            rootLogger.info("Verbose output enabled");
        } else {
            rootLogger.setLevel(Level.OFF);
        }

        rootLogger.info("Version: " + this.version);

        if (cmd.hasOption('h')) {
            this.printHelp();;
            System.exit(0);
        }

        if (cmd.hasOption("help")) {
            setHelp(true);
            final String commandString = cmd.getOptionValue("help");
            try {
                if (commandString == null) {
                    this.setCommand(null);
                } else {
                    this.setCommand(CommandValue.valueOf(commandString.toUpperCase()));
                }
                // no other options count
                return;
            } catch (final IllegalArgumentException e) {
                throw new BadArgumentException("Unknown command: " + commandString + "; use -h to get available commands.", e);
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

        if (cmd.hasOption("insecure")) {
            this.setCertificateVerification(false);
        }

        if (cmd.hasOption("http")) {
            this.setHttps(false);
        }

        final String retryString = cmd.getOptionValue("r");
        try {
            if (retryString != null) {
                this.setRetries(Integer.parseInt(retryString));
            }

        } catch (final NumberFormatException e) {
            System.err.printf("Error: Argument (%s) to '-r' was not a number\n", retryString);
            System.exit(1);
        }

        try {
            final String commandString = cmd.getOptionValue("c");
            if (commandString == null) {
                this.setCommand(null);
                // must have -c or --help
                if (!isHelp()) {
                    missingArgs.add("c");
                }
            } else {
                this.setCommand(CommandValue.valueOf(commandString.toUpperCase()));
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown command", e);
        }

        this.setPriority(this.processPriorityType(cmd, "priority"));

        this.setWriteOptimization(this.processWriteOptimization(cmd, "writeOptimization"));

        if (cmd.hasOption("force")) {
            this.setForce(true);
        }

        if (cmd.hasOption("checksum")) {
            this.setChecksum(true);
        }

        if (cmd.hasOption("completed")) {
            this.setCompleted(true);
        }

        this.setBucket(cmd.getOptionValue("b"));
        this.setEndpoint(cmd.getOptionValue("e"));
        this.setAccessKey(cmd.getOptionValue("a"));
        this.setSecretKey(cmd.getOptionValue("k"));
        this.setObjectName(cmd.getOptionValue("o"));
        this.setProxy(cmd.getOptionValue("x"));
        this.setDirectory(cmd.getOptionValue("d"));
        this.setPrefix(cmd.getOptionValue("p"));
        this.setId(cmd.getOptionValue("i"));

        if (this.getEndpoint() == null) {
            final String endpoint = System.getenv("DS3_ENDPOINT");
            if (endpoint == null) {
                missingArgs.add("e");
            } else {
                this.setEndpoint(endpoint);
            }
        }

        if (this.getSecretKey() == null) {
            final String key = System.getenv("DS3_SECRET_KEY");
            if (key == null) {
                missingArgs.add("k");
            } else {
                this.setSecretKey(key);
            }
        }

        if (this.getAccessKey() == null) {
            final String key = System.getenv("DS3_ACCESS_KEY");
            if (key == null) {
                missingArgs.add("a");
            } else {
                this.setAccessKey(key);
            }
        }

        // check for the http_proxy env var
        final String proxy = System.getenv("http_proxy");
        if (proxy != null) {
            this.setProxy(proxy);
            LOG.info("Proxy: %s", this.getProxy());
        }

        if (!missingArgs.isEmpty()) {
            throw new MissingOptionException(missingArgs);
        }
        LOG.info("Access Key: " + this.getAccessKey() + " | Endpoint: " + this.getEndpoint());

        if (cmd.hasOption("sync")) {
            this.setSync(true);
        }

        this.setNumberOfFiles(cmd.getOptionValue("n"));
        this.setSizeOfFiles(cmd.getOptionValue("s"));

        this.setBufferSize(cmd.getOptionValue("bs"));
        if (this.getBufferSize() == null) {
            this.bufferSize = "1048576"; //default to 1MB
        }

        this.setNumberOfThreads(cmd.getOptionValue("nt"));
        if (this.getNumberOfThreads() == null) {
            this.numberOfThreads = "10"; //default to 10 threads
        }

        if (cmd.hasOption("ignore-errors")) {
            this.setIgnoreErrors(true);
        }

        if (cmd.hasOption("follow-symlinks")) {
            this.setFollowSymlinks(true);
        }

        if (cmd.hasOption("no-follow-symlinks")) {
            this.setFollowSymlinks(false);
        }

        if (cmd.hasOption("metadata")) {
            this.setMetadata(Metadata.parse(cmd.getOptionValues("metadata")));
        }
        if (cmd.hasOption("modify-params")) {
            this.setModifyParams(Metadata.parse(cmd.getOptionValues("modify-params")));
        }

        if (cmd.hasOption("discard")) {
            this.setDiscard(true);
        }
    }

    private WriteOptimization processWriteOptimization(final CommandLine cmd, final String writeOptimization) throws BadArgumentException {
        final String writeOptimizationString = cmd.getOptionValue(writeOptimization);
        try {
            if (writeOptimizationString == null) {
                return null;
            } else {
                return WriteOptimization.valueOf(writeOptimizationString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown writeOptimization: " + writeOptimizationString, e);
        }
    }

    private Priority processPriorityType(final CommandLine cmd, final String priority) throws BadArgumentException {
        final String priorityString = cmd.getOptionValue(priority);
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

    public String getVersion() {
        return this.version;
    }

    public String getBuildDate() {
        return this.buildDate;
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
                if (LOG.isInfoEnabled()) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printVersion() {
        System.out.println("Version: " + this.getVersion());
        System.out.println("Build Date: " + this.getBuildDate());
    }

    public void printHelp() {
        // default help menu
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("ds3", this.options);
    }

    public String getBucket() {
        return this.bucket;
    }

    void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getDirectory() {
        return this.directory;
    }

    void setDirectory(final String directory) {
        this.directory = directory;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public CommandValue getCommand() {
        return this.command;
    }

    void setCommand(final CommandValue command) {
        this.command = command;
    }

    void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    public String getObjectName() {
        return this.objectName;
    }

    void setProxy(final String proxy) {
        this.proxy = proxy;
    }

    public String getProxy() {
        return this.proxy;
    }

    void setForce(final boolean force) {
        this.force = force;
    }

    public boolean isForce() {
        return this.force;
    }

    void setRetries(final int retries) {
        this.retries = retries;
    }

    public int getRetries() {
        return this.retries;
    }

    public String getPrefix() {
        return this.prefix;
    }

    void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public boolean isChecksum() {
        return this.checksum;
    }

    void setChecksum(final boolean checksum) {
        this.checksum = checksum;
    }

    public Priority getPriority() {
        return this.priority;
    }

    void setPriority(final Priority priority) {
        this.priority = priority;
    }

    public WriteOptimization getWriteOptimization() {
        return this.writeOptimization;
    }

    void setWriteOptimization(final WriteOptimization writeOptimization) {
        this.writeOptimization = writeOptimization;
    }

    public boolean isCertificateVerification() {
        return this.certificateVerification;
    }

    void setCertificateVerification(final boolean certificateVerification) {
        this.certificateVerification = certificateVerification;
    }

    public boolean isHttps() {
        return this.https;
    }

    void setHttps(final boolean https) {
        this.https = https;
    }

    public ViewType getOutputFormat() {
        return this.outputFormat;
    }

    void setOutputFormat(final ViewType outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getId() {
        return this.id;
    }

    void setId(final String id) {
        this.id = id;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    void setCompleted(final boolean completed) {
        this.completed = completed;
    }

    void setSync(final boolean sync) {
        this.sync = sync;
    }

    public boolean isSync() {
        return this.sync;
    }

    void setNumberOfFiles(final String numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public String getNumberOfFiles() {
        return this.numberOfFiles;
    }

    void setSizeOfFiles(final String sizeOfFiles) {
        this.sizeOfFiles = sizeOfFiles;
    }

    public String getSizeOfFiles() {
        return this.sizeOfFiles;
    }

    void setBufferSize(final String bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getBufferSize() {
        return this.bufferSize;
    }

    void setNumberOfThreads(final String numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getNumberOfThreads() {
        return this.numberOfThreads;
    }

    public boolean isIgnoreErrors() {
        return this.ignoreErrors;
    }

    void setIgnoreErrors(final boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }

    public boolean isFollowSymlinks() {
        return followSymlinks;
    }

    void setFollowSymlinks(final boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
    }

    public ImmutableMap<String, String> getMetadata() {
        return metadata;
    }

    public ImmutableMap<String, String> getModifyParams() {
        return modifyParams;
    }

    void setMetadata(final ImmutableMap<String, String> metadata) {
        this.metadata = metadata;
    }

    void setModifyParams(final ImmutableMap<String, String> modifyParams) { this.modifyParams = modifyParams; }

    public boolean isDiscard() {
        return discard;
    }

    void setDiscard(final boolean discard) {
        this.discard = discard;
    }

    public boolean isHelp() { return this.help; }

    void setHelp(final boolean help) { this.help = help; }

}

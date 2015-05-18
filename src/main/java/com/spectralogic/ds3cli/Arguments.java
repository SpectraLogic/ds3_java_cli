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
import com.spectralogic.ds3client.models.bulk.Priority;
import com.spectralogic.ds3client.models.bulk.WriteOptimization;
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

    private final static String PROPERTY_FILE = "config.properties";

    private final Options options;

    private String bucket;
    private String directory;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String prefix;
    private String id = null;
    private CommandValue command;
    private final String[] args;
    private String objectName;
    private String proxy;
    private int start;
    private int end;
    private int retries = 20;
    private Priority priority;
    private Priority defaultPutPriority;
    private Priority defaultGetPriority;
    private WriteOptimization writeOptimization;
    private WriteOptimization defaultWriteOptimization;
    private boolean clearBucket = false;
    private boolean checksum = false;
    private boolean certificateVerification = true;
    private boolean https = true;
    private ViewType outputFormat = ViewType.CLI;

    private String version = "N/a";
    private String buildDate = "N/a";

    Arguments(final String[] args) throws BadArgumentException, ParseException {
        loadProperties();
        this.args = args;
        options = new Options();

        final Option ds3Endpoint = new Option("e", true, "The ds3 endpoint to connect to or have \"DS3_ENDPOINT\" set as an environment variable.");
        ds3Endpoint.setArgName("endpoint");
        final Option bucket = new Option("b", true, "The ds3 bucket to copy to");
        bucket.setArgName("bucket");
        final Option accessKey = new Option("a", true, "Access Key ID or have \"DS3_ACCESS_KEY\" set as an environment variable");
        accessKey.setArgName("accessKeyId");
        final Option secretKey = new Option("k", true, "Secret access key or have \"DS3_SECRET_KEY\" set as an environment variable");
        secretKey.setArgName("secretKey");
        final Option command = new Option("c", true, "The Command to execute.  Possible values: [" + CommandValue.valuesString() + "]");
        command.setArgName("command");
        final Option directory = new Option("d", true, "Specify a directory to interact with if required");
        directory.setArgName("directory");
        final Option objectName = new Option("o", true, "The name of the object to be retrieved or stored");
        objectName.setArgName("objectFileName");
        final Option prefix = new Option("p", true, "Restores only objects who's names start with prefix.  Used with `get_bulk`");
        prefix.setArgName("prefix");
        final Option proxy = new Option("x", true, "The URL of the proxy server to use or have \"http_proxy\" set as an environment variable");
        proxy.setArgName("proxy");
        final Option start = new Option("s", true, "The starting byte for a get_object command");
        start.setArgName("start");
        final Option end = new Option("n", true, "The ending byte for a get_object command");
        end.setArgName("end");
        final Option id = new Option("i", true, "ID for identifying ds3 api resources");
        id.setArgName("id");
        final Option clearBucket = new Option(null, false, "Used with the command `delete_bucket`.  If this is set then the `delete_bucket` command will also delete all the objects in the bucket");
        clearBucket.setLongOpt("force");
        final Option retries = new Option("r", true, "Specifies how many times puts and gets will be attempted before failing the request.  The default is 5");
        retries.setArgName("retries");
        final Option checksum = new Option(null, "Validate checksum values");
        checksum.setLongOpt("checksum");
        final Option priority = new Option(null, true, "Set the bulk job priority.  Possible values: [" + Priority.valuesString() + "]");
        priority.setLongOpt("priority");
        priority.setArgName("priority");
        final Option defaultPutPriority = new Option(null, true, "Set the default bulk put job priority.  Possible values: [" + Priority.valuesString() + "]");
        defaultPutPriority.setLongOpt("defaultPutPriority");
        defaultPutPriority.setArgName("priority");
        final Option defaultGetPriority = new Option(null, true, "Set the default bulk get job priority.  Possible values: [" + Priority.valuesString() + "]");
        defaultGetPriority.setLongOpt("defaultGetPriority");
        defaultGetPriority.setArgName("priority");
        final Option writeOptimization = new Option(null, true, "Set the job write optimization.  Possible values: ["+ WriteOptimization.valuesString() +"]");
        writeOptimization.setLongOpt("writeOptimization");
        writeOptimization.setArgName("writeOptimization");
        final Option defaultWriteOptimization = new Option(null, true, "Set the default job write optimization for a bucket.  Possible values: ["+ WriteOptimization.valuesString() +"]");
        defaultWriteOptimization.setLongOpt("defaultWriteOptimization");
        defaultWriteOptimization.setArgName("writeOptimization");
        final Option help = new Option("h", "Print Help Menu");
        help.setLongOpt("help");
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
        final Option viewType = new Option(null, true, "Configure how the output should be displayed.  Possible values: ["+ ViewType.valuesString() +"]");
        viewType.setLongOpt("output-format");
        options.addOption(ds3Endpoint);
        options.addOption(bucket);
        options.addOption(directory);
        options.addOption(accessKey);
        options.addOption(secretKey);
        options.addOption(command);
        options.addOption(objectName);
        options.addOption(prefix);
        options.addOption(proxy);
        options.addOption(id);
        //options.addOption(start);  //TODO re-add these calls when we have support for partial file gets in the helper functions
        //options.addOption(end);
        options.addOption(clearBucket);
        options.addOption(retries);
        options.addOption(checksum);
        options.addOption(priority);
        options.addOption(writeOptimization);
        options.addOption(help);
        options.addOption(insecure);
        options.addOption(http);
        options.addOption(version);
        options.addOption(verbose);
        options.addOption(debug);
        options.addOption(trace);
        options.addOption(viewType);

        // Disabled until they are enabled in DS3.
        // options.addOption(defaultGetPriority);
        // options.addOption(defaultPutPriority);
        // options.addOption(defaultWriteOptimization);

        processCommandLine();
    }

    private void processCommandLine() throws ParseException, BadArgumentException {
        final CommandLineParser parser = new BasicParser();
        final CommandLine cmd = parser.parse(options, args);

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
            printHelp();
            System.exit(0);
        }

        if (cmd.hasOption("version")) {
            printVersion();
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
            setCertificateVerification(false);
        }

        if (cmd.hasOption("http")) {
            setHttps(false);
        }

        final String retryString = cmd.getOptionValue("r");
        try {
            if (retryString != null) {
                setRetries(Integer.parseInt(retryString));
            }

        } catch (final NumberFormatException e) {
            System.err.printf("Error: Argument (%s) to '-r' was not a number\n", retryString);
            System.exit(1);
        }

        try {
            final String commandString = cmd.getOptionValue("c");
            if (commandString == null) {
                this.setCommand(null);
                missingArgs.add("c");
            } else {
                this.setCommand(CommandValue.valueOf(commandString.toUpperCase()));
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown command", e);
        }

        this.setPriority(processPriorityType(cmd, "priority"));
        this.setDefaultGetPriority(processPriorityType(cmd, "defaultGetPriority"));
        this.setDefaultPutPriority(processPriorityType(cmd, "defaultPutPriority"));

        this.setDefaultWriteOptimization(processWriteOptimization(cmd, "defaultWriteOptimization"));
        this.setWriteOptimization(processWriteOptimization(cmd, "writeOptimization"));

        if (cmd.hasOption("force")) {
            this.setClearBucket(true);
        }

        if (cmd.hasOption("checksum")) {
            this.setChecksum(true);
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

        final String start = cmd.getOptionValue("s");
        if (!(start == null || start.isEmpty())) {
            this.setStart(Integer.parseInt(start));
        }
        final String end = cmd.getOptionValue("n");
        if (!(end == null || end.isEmpty())) {
            this.setEnd(Integer.parseInt(end));
        }


        if (getEndpoint() == null) {
            final String endpoint = System.getenv("DS3_ENDPOINT");
            if (endpoint == null) {
                missingArgs.add("e");
            } else {
                setEndpoint(endpoint);
            }
        }

        if (getSecretKey() == null) {
            final String key = System.getenv("DS3_SECRET_KEY");
            if (key == null) {
                missingArgs.add("k");
            } else {
                setSecretKey(key);
            }
        }

        if (getAccessKey() == null) {
            final String key = System.getenv("DS3_ACCESS_KEY");
            if (key == null) {
                missingArgs.add("a");
            } else {
                setAccessKey(key);
            }
        }

        // check for the http_proxy env var
        final String proxy = System.getenv("http_proxy");
        if (proxy != null) {
            setProxy(proxy);
            LOG.info("Proxy: %s", getProxy());
        }

        if (!missingArgs.isEmpty()) {
            throw new MissingOptionException(missingArgs);
        }
        LOG.info("Access Key: " + getAccessKey() +" | Secret Key: " + getSecretKey() + " | Endpoint: " + getEndpoint());
    }

    private WriteOptimization processWriteOptimization(final CommandLine cmd, final String writeOptimization) throws BadArgumentException {
        final String writeOptimizationString = cmd.getOptionValue(writeOptimization);
        try {
            if (writeOptimizationString == null) {
                return null;
            }
            else {
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
            }
            else {
                return Priority.valueOf(priorityString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown priority: " + priorityString, e);
        }
    }

    public String getVersion() {
        return version;
    }

    public String getBuildDate() {
        return buildDate;
    }

    private void loadProperties() {
        final Properties props = new Properties();
        final InputStream input = Arguments.class.getClassLoader().getResourceAsStream(PROPERTY_FILE);
        if (input == null) {
            System.err.println("Could not find property file.");
        }
        else {
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
        System.out.println("Version: " + getVersion() );
        System.out.println("Build Date: " + getBuildDate());
    }

    public void printHelp() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("ds3", options);
    }

    public String getBucket() {
        return bucket;
    }

    void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getDirectory() {
        return directory;
    }

    void setDirectory(final String directory) {
        this.directory = directory;
    }

    public String getEndpoint() {
        return endpoint;
    }

    void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public CommandValue getCommand() {
        return command;
    }

    void setCommand(final CommandValue command) {
        this.command = command;
    }

    void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    void setProxy(final String proxy) {
        this.proxy = proxy;
    }

    public String getProxy() {
        return this.proxy;
    }

    public int getStart() {
        return start;
    }

    void setStart(final int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    void setEnd(final int end) {
        this.end = end;
    }

    void setClearBucket(boolean clearBucket) {
        this.clearBucket = clearBucket;
    }

    public boolean isClearBucket() {
        return this.clearBucket;
    }

    void setRetries(int retries) {
        this.retries = retries;
    }

    public int getRetries() {
        return this.retries;
    }

    public String getPrefix() {
        return prefix;
    }

    void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public boolean isChecksum() {
        return checksum;
    }

    void setChecksum(final boolean checksum) {
        this.checksum = checksum;
    }

    public Priority getPriority() {
        return priority;
    }

    void setPriority(final Priority priority) {
        this.priority = priority;
    }

    public WriteOptimization getWriteOptimization() {
        return writeOptimization;
    }

    void setWriteOptimization(final WriteOptimization writeOptimization) {
        this.writeOptimization = writeOptimization;
    }

    public Priority getDefaultPutPriority() {
        return defaultPutPriority;
    }

    void setDefaultPutPriority(final Priority defaultPutPriority) {
        this.defaultPutPriority = defaultPutPriority;
    }

    public Priority getDefaultGetPriority() {
        return defaultGetPriority;
    }

    void setDefaultGetPriority(final Priority defaultGetPriority) {
        this.defaultGetPriority = defaultGetPriority;
    }

    public WriteOptimization getDefaultWriteOptimization() {
        return defaultWriteOptimization;
    }

    void setDefaultWriteOptimization(final WriteOptimization defaultWriteOptimization) {
        this.defaultWriteOptimization = defaultWriteOptimization;
    }

    public boolean isCertificateVerification() {
        return certificateVerification;
    }

    void setCertificateVerification(final boolean certificateVerification) {
        this.certificateVerification = certificateVerification;
    }

    public boolean isHttps() {
        return https;
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
        return id;
    }

    void setId(final String id) {
        this.id = id;
    }
}

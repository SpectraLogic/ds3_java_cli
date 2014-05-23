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

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;

public class Arguments {

    private final Options options;

    private String bucket;
    private String directory;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private CommandValue command;
    private final String[] args;
    private String objectName;
    private String proxy;

    Arguments(final String[] args) throws BadArgumentException, ParseException {
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
        final Option directory = new Option("d", true, "Specify a directory to interact with if required.");
        directory.setArgName("directory");
        final Option objectName = new Option("o", true, "The name of the object to be retrieved or stored");
        objectName.setArgName("objectFileName");
        final Option proxy = new Option("x", true, "The URL of the proxy server to use.");
        proxy.setArgName("proxy");
        final Option help = new Option("h", "Print Help Menu");

        options.addOption(ds3Endpoint);
        options.addOption(bucket);
        options.addOption(directory);
        options.addOption(accessKey);
        options.addOption(secretKey);
        options.addOption(command);
        options.addOption(objectName);
        options.addOption(proxy);
        options.addOption(help);

        processCommandLine();
    }

    private void processCommandLine() throws ParseException, BadArgumentException {

        final CommandLineParser parser = new BasicParser();
        final CommandLine cmd = parser.parse(options, args);

        final List<String> missingArgs = new ArrayList<>();

        if (cmd.hasOption('h')) {
            printHelp();
            System.exit(0);
        }

        try {
            final String commandString = cmd.getOptionValue("c");
            if (commandString == null) {
                this.setCommand(null);
                missingArgs.add("c");
            } else {
                this.setCommand(CommandValue.valueOf(commandString.toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            throw new BadArgumentException("Unknown command", e);
        }

        this.setBucket(cmd.getOptionValue("b"));
        this.setEndpoint(cmd.getOptionValue("e"));
        this.setAccessKey(cmd.getOptionValue("a"));
        this.setSecretKey(cmd.getOptionValue("k"));
        this.setObjectName(cmd.getOptionValue("o"));
        this.setProxy(cmd.getOptionValue("x"));
        this.setDirectory(cmd.getOptionValue("d"));



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

        if (!missingArgs.isEmpty()) {
            throw new MissingOptionException(missingArgs);
        }
    }

    public void printHelp() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("ds3", options);
    }

    public String getBucket() {
        return bucket;
    }

    private void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getDirectory() {
        return directory;
    }

    private void setDirectory(final String directory) {
        this.directory = directory;
    }

    public String getEndpoint() {
        return endpoint;
    }

    private void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    private void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    private void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public CommandValue getCommand() {
        return command;
    }

    private void setCommand(final CommandValue command) {
        this.command = command;
    }

    private void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    private void setProxy(final String proxy) {
        this.proxy = proxy;
    }

    public String getProxy() {
        return this.proxy;
    }
}

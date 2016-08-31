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

public class ArgumentFactory {

    // always added and read in first parsing
    public final static Option endpoint = new Option("e", true, "The ds3 endpoint to connect to or have \"DS3_ENDPOINT\" set as an environment variable.");
    public final static Option accessKey = new Option("a", true, "Access Key ID or have \"DS3_ACCESS_KEY\" set as an environment variable");
    public final static Option secretKey = new Option("k", true, "Secret access key or have \"DS3_SECRET_KEY\" set as an environment variable");
    public final static Option command = new Option("c", true, "The Command to execute.  For Possible values, use '--help list_commands.'" );
    public final static Option proxy = new Option("x", true, "The URL of the proxy server to use or have \"http_proxy\" set as an environment variable");
    public final static Option http = Option.builder().longOpt("http").desc("Send all requests over standard http").build();
    public final static Option insecure = Option.builder().longOpt("insecure").desc("Ignore ssl certificate verification").build();
    public final static Option printHelp = new Option("h", "Help Menu");
    public final static Option commandHelp = Option.builder()
            .longOpt("help")
            .desc("Command Help (provide command name from -c)")
            .optionalArg(true)
            .numberOfArgs(1)
            .build();
    public static final Option verbose = Option.builder().longOpt("verbose").desc("Log output to console.").build();
    public static final Option debug = Option.builder().longOpt("debug").desc("Debug (more verbose) output to console.").build();
    public static final Option trace = Option.builder().longOpt("trace").desc("Trace (most verbose) output to console.").build();
    public static final Option logVerbose = Option.builder().longOpt("log-verbose").desc("Log output to log file.").build();
    public static final Option logDebug = Option.builder().longOpt("log-debug").desc("Debug (more verbose) output to log file.").build();
    public static final Option logTrace = Option.builder().longOpt("log-trace").desc("Trace (most verbose) output to log file.").build();
    public static final Option printVersion = Option.builder().longOpt("version").desc("Print version information").build();
    public static final Option viewType = Option.builder().longOpt("output-format")
            .desc("Configure how the output should be displayed.  Possible values: [" + ViewType.valuesString() + "]").build();
    public static final Option retries = new Option("r", true,
            "Specifies how many times puts and gets will be attempted before failing the request. The default is 5");
    public static final Option bufferSize = new Option("bs", true, "Set the buffer size in bytes. The defalut is 1MB");

    // added as needed by commands
    public static final Option bucket = Option.builder("b").hasArg(true)
            .desc("The ds3 bucket to copy to").argName("bucketName").build();
    public static final Option directory = Option.builder("d").hasArg(true)
            .desc("Specify a directory to interact with if required").argName("directoty").build();
    public static final Option prefix = Option.builder("p").hasArg(true).argName("prefix")
            .desc("Used with get operations to restore only objects whose names start with prefix  "
             + "and with put operations, to prepend a prefix to object name(s)").build();
    public static final Option objectName = Option.builder("o").hasArg(true).argName("objectFileName")
            .desc("The name of the object to be retrieved or stored").build();
    public static final Option id = Option.builder("i").hasArg(true).argName("optionId")
            .desc("ID for identifying ds3 api resources").build();
    public static final Option force = Option.builder().longOpt("force")
            .desc("Used to force an operation even if there is an error").build();
    public static final Option checksum = Option.builder().longOpt("checksum").desc("Validate checksum values").build();
    public static final Option writeOptimization = Option.builder().longOpt("writeOptimization")
            .desc("Set the job write optimization.  Possible values: ["
             + Utils.printEnumOptions(WriteOptimization.values()) + "]").build();
    public static final Option completed = Option.builder().longOpt("completed")
            .desc("Used with the command get_jobs to include the display of completed jobs").build();
    public static final Option sync = Option.builder().longOpt("sync").desc("Copy only the newest files").build();
    public static final Option numberOfFiles = Option.builder("n").hasArg(true).argName("numberOfFiles")
            .desc("The number of files for the performance test").build();
    public static final Option sizeOfFiles = Option.builder("s").hasArg(true).argName("sizeOfFiles")
            .desc("The size (in MB) for each file in the performance test").build();
    public static final Option numberOfThreads = Option.builder("nt").hasArg(true).argName("number")
            .desc("Set the number of threads").build();
    public static final Option ignoreErrors = Option.builder().longOpt("ignore-errors").desc("Ignore files that cause errors").build();
    public static final Option noFollowSymlinks = Option.builder().longOpt("follow-symlinks")
            .desc("Set to not follow symlinks, this is the default behavior").build();
    public static final Option followSymLinks = Option.builder().longOpt("no-follow-symlinks").desc("Set to follow symlinks").build();
    public static final Option discard = Option.builder().longOpt("discard").desc("Discard restoration data (/dev/null)").build();
    public static final Option metadata = Option.builder()
            .longOpt("metadata")
            .desc("Metadata for when putting a single object.  Using the format: key:value,key2:value2")
            .hasArgs()
            .valueSeparator(',')
            .build();
    public static final Option modifyParams = Option.builder()
            .longOpt("modify-params")
            .desc("Parameters for modifying features using the format key:value,key2:value2. For modify_user: default_data_policy_id")
            .hasArgs()
            .valueSeparator(',')
            .build();
    public static final Option priority = Option.builder().
            longOpt("priority")
            .desc("Set the bulk job priority.  Possible values: [" + Utils.printEnumOptions(Priority.values()) + "]")
            .hasArgs()
            .build();
    public static final Option verifyPercent = Option.builder().longOpt("verify-last-percent").argName("percent")
            .desc("Set verify last percent as an integer.  Used with modify_data_path").build();
    public static final Option ignoreNamingConflicts = Option.builder().longOpt("ignore-naming-conflicts")
            .desc("Set true to ignore existing files of the same name and size during a bulk put").build();
    public static final Option inCache = Option.builder().longOpt("in-cache")
            .desc("Set to filter out items that are only in cache.  Used with get_suspect_objects").build();


}




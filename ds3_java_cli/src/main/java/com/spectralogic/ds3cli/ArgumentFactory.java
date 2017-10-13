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

import com.spectralogic.ds3cli.util.CliUtils;
import com.spectralogic.ds3client.models.Priority;
import com.spectralogic.ds3client.models.WriteOptimization;
import org.apache.commons.cli.Option;

public final class ArgumentFactory {

    // added by base Arguments class and available in first parsing
    // These options are added in the Arguments class and read in first parsing
    public final static Option ENDPOINT = new Option("e", true, "The ds3 endpoint to connect to or have \"DS3_ENDPOINT\" set as an environment variable.");
    public final static Option ACCESS_KEY = new Option("a", true, "Access Key ID or have \"DS3_ACCESS_KEY\" set as an environment variable");
    public final static Option SECRET_KEY = new Option("k", true, "Secret access key or have \"DS3_SECRET_KEY\" set as an environment variable");
    public final static Option COMMAND = new Option("c", true, "The Command to execute.  For Possible values, use '--help list_commands.'" );
    public final static Option PROXY = new Option("x", true, "The URL of the PROXY server to use or have \"http_proxy\" set as an environment variable");
    public final static Option HTTP = Option.builder().longOpt("http").desc("Send all requests over standard HTTP").build();
    public final static Option INSECURE = Option.builder().longOpt("insecure").desc("Ignore ssl certificate verification").build();
    public final static Option PRINT_HELP = new Option("h", "Help Menu");
    public final static Option COMMAND_HELP = Option.builder()
            .longOpt("help")
            .desc("Command Help (provide command name from -c)")
            .optionalArg(true)
            .numberOfArgs(1)
            .build();
    public static final Option VERBOSE = Option.builder().longOpt("verbose").desc("Log output to console.").build();
    public static final Option DEBUG = Option.builder().longOpt("debug").desc("Debug (more verbose) output to console.").build();
    public static final Option TRACE = Option.builder().longOpt("trace").desc("Trace (most verbose) output to console.").build();
    public static final Option LOG_VERBOSE = Option.builder().longOpt("log-verbose").desc("Log output to log file.").build();
    public static final Option LOG_DEBUG = Option.builder().longOpt("log-debug").desc("Debug (more verbose) output to log file.").build();
    public static final Option LOG_TRACE = Option.builder().longOpt("log-trace").desc("Trace (most verbose) output to log file.").build();
    public static final Option PRINT_VERSION = Option.builder().longOpt("version").desc("Print version information").build();
    public static final Option RETRIES = new Option("r", true,
            "Specifies how many times puts and gets will be attempted before failing the request. The default is 5");
    public static final Option BUFFER_SIZE = new Option("bs", true, "Set the buffer size in bytes. The default is 1MB");

    // added as needed by commands
    public static final Option BUCKET = Option.builder("b").hasArg(true)
            .desc("The ds3 bucket").argName("bucketName").build();
    public static final Option DIRECTORY = Option.builder("d").hasArg(true)
            .desc("Specify a directory to interact with if required").argName("directory").build();
    public static final Option PREFIX = Option.builder("p").hasArg(true).argName("prefix")
            .desc("Used with get operations to restore only objects whose names start with prefix  "
                    + "and with put operations, to prepend a prefix to object name(s)").build();
    public static final Option OBJECT_NAME = Option.builder("o").hasArg(true).argName("objectFileName")
            .desc("The name of the object to be retrieved or stored").build();
    public static final Option ID = Option.builder("i").hasArg(true).argName("optionId")
            .desc("ID for identifying ds3 api resources").build();
    public static final Option FORCE = Option.builder().longOpt("force")
            .desc("Used to force an operation even if there is an error").build();
    public static final Option CHECKSUM = Option.builder().longOpt("checksum").desc("Validate checksum values").build();
    public static final Option WRITE_OPTIMIZATION = Option.builder().longOpt("writeOptimization")
            .desc("Set the job write optimization.  Possible values: ["
                    + CliUtils.printEnumOptions(WriteOptimization.values()) + "]").build();
    public static final Option COMPLETED = Option.builder().longOpt("completed")
            .desc("Used with the command get_jobs to include the display of completed jobs").build();
    public static final Option SYNC = Option.builder().longOpt("sync").desc("Copy only the newest files").build();
    public static final Option NUMBER_OF_FILES = Option.builder("n").hasArg(true).argName("number_of_files")
            .desc("The number of files for the performance test").build();
    public static final Option SIZE_OF_FILES = Option.builder("s").hasArg(true).argName("size_of_files")
            .desc("The size (in MB) for each file in the performance test").build();
    public static final Option NUMBER_OF_THREADS = Option.builder("nt").hasArg(true).argName("number")
            .desc("Set the number of threads").build();
    public static final Option IGNORE_ERRORS = Option.builder().longOpt("ignore-errors").desc("Ignore files that cause errors").build();
    public static final Option NO_FOLLOW_SYMLINKS = Option.builder().longOpt("follow-symlinks")
            .desc("Set to not follow symlinks, this is the default behavior").build();
    public static final Option FOLLOW_SYMLINKS = Option.builder().longOpt("no-follow-symlinks").desc("Set to follow symlinks").build();
    public static final Option DISCARD = Option.builder().longOpt("discard").desc("Discard restoration data (/dev/null)").build();
    public static final Option USER_METADATA = Option.builder()
            .longOpt("user-metadata")
            .desc("Metadata for when putting a single object.  Using the format: key:value,key2:value2")
            .hasArgs()
            .valueSeparator(',')
            .build();
    public static final Option FILE_METADATA = Option.builder()
            .longOpt("file-metadata")
            .desc("Archive and restore file metadata, such as modified and accessed times, permissions and ownership.")
            .build();
    public static final Option MODIFY_PARAMS = Option.builder()
            .longOpt("modify-params")
            .desc("Parameters for modifying features using the format key:value,key2:value2. For modify_user: default_data_policy_id")
            .hasArgs()
            .valueSeparator(',')
            .build();
    public static final Option FILTER_PARAMS = Option.builder()
            .longOpt("filter-params")
            .desc("Parameters for filtering detailed objects using the format key:value,key2:value2. ")
            .hasArgs()
            .valueSeparator(',')
            .build();
    public static final Option PRIORITY = Option.builder().
            longOpt("priority")
            .desc("Set the bulk job priority.  Possible values: [" + CliUtils.printEnumOptions(Priority.values()) + "]")
            .hasArg(true)
            .build();
    public static final Option VERIFY_PERCENT = Option.builder().longOpt("verify-last-percent").argName("percent")
            .desc("Set verify last percent as an integer.").build();
    public static final Option IGNORE_NAMING_CONFLICTS = Option.builder().longOpt("ignore-naming-conflicts")
            .desc("Set true to ignore existing files of the same name and size during a bulk put").build();
    public static final Option IN_CACHE = Option.builder().longOpt("in-cache")
            .desc("Set to filter out items that are only in cache.  Used with get_suspect_objects").build();
    public static final Option VIEW_TYPE = Option.builder().longOpt("output-format").hasArg()
            .desc("Configure how the output should be displayed.  Possible values: [cli, json]").build();
    public static final Option EJECT_LABEL = Option.builder().longOpt("eject-label").hasArg()
            .desc("Enter information to assist in the handling of the tapes.").build();
    public static final Option EJECT_LOCATION = Option.builder().longOpt("eject-location").hasArg()
            .desc("Enter information to describe where the ejected tapes can be located.").build();
    public static final Option RANGE_OFFSET = Option.builder().
            longOpt("range-offset")
            .desc("Set the start point (long) for partial object recovery")
            .hasArg(true)
            .build();
    public static final Option RANGE_LENGTH = Option.builder().
            longOpt("range-length")
            .desc("Set the length in bytes for partial object recovery; use with " + RANGE_OFFSET.getLongOpt())
            .hasArg(true)
            .build();

}




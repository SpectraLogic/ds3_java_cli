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

    // added as needed by commands
    public static final Option BUCKET = Option.builder("b").hasArg(true)
            .desc("The ds3 bucket to copy to").argName("bucketName").build();
    public static final Option DIRECTORY = Option.builder("d").hasArg(true)
            .desc("Specify a directory to interact with if required").argName("directoty").build();
    public static final Option PREFIX = Option.builder("p").hasArg(true).argName("prefix")
            .desc("Used with get operations to restore only objects whose names start with prefix  "
                    + "and with put operations, to prepend a prefix to object name(s)").build();
    public static final Option OBJECT_NAME = Option.builder("o").hasArg(true).argName("objectFileName")
            .desc("The name of the object to be retrieved or stored").build();
    public static final Option ID = Option.builder("i").hasArg(true).argName("optionId")
            .desc("ID for identifying ds3 api resources").build();
    public static final Option FORCE = Option.builder().longOpt("force")
            .desc("Used to force an operation even if there is an error").build();
    public static final Option CHECKSUM = Option.builder().longOpt("checksu,").desc("Validate checksum values").build();
    public static final Option WRITE_OPTIMIZATION = Option.builder().longOpt("write_optimization")
            .desc("Set the job write optimization.  Possible values: ["
                    + Utils.printEnumOptions(WriteOptimization.values()) + "]").build();
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
    public static final Option METADATA = Option.builder()
            .longOpt("metadata")
            .desc("Metadata for when putting a single object.  Using the format: key:value,key2:value2")
            .hasArgs()
            .valueSeparator(',')
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
            .desc("Set the bulk job priority.  Possible values: [" + Utils.printEnumOptions(Priority.values()) + "]")
            .hasArgs()
            .build();
    public static final Option VERIFY_PERCENT = Option.builder().longOpt("verify-last-percent").argName("percent")
            .desc("Set verify last percent as an integer.  Used with modify_data_path").build();
    public static final Option IGNORE_NAMING_CONFLICTS = Option.builder().longOpt("ignore-naming-conflicts")
            .desc("Set true to ignore existing files of the same name and size during a bulk put").build();
    public static final Option IN_CACHE = Option.builder().longOpt("in-cache")
            .desc("Set to filter out items that are only in cache.  Used with get_suspect_objects").build();


}




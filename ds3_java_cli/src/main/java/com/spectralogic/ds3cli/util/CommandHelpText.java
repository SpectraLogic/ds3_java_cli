/*
 * ******************************************************************************
 *   Copyright 2014 - 2016 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3cli.CommandValue;

public class CommandHelpText {

    public static String getCommandText(CommandValue command) {

        switch(command) {
            case GET_OBJECT: {
                return "Retrieves a single object from a bucket.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "Requires the '-o' parameter to specify object (name or UUID).\n"
                + "Optional '-d' parameter to specify restore directory (default '.').\n"
                + "Optional '--sync' flag to retrieve onnly newer or non-extant files.\n"
                + "Optional '-nt' parameter to specify number of threads.\n"
                + "\nUse the get_service command to retrieve a list of buckets."
                + "\nUse the get_bucket command to retrieve a list of objects.";
            }
            case GET_BUCKET: {
                return "Returns a list of objects contained in a bucket.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "\nUse the get_service command to retrieve a list of buckets";
            }
            case PUT_BUCKET: {
                return "Create a new empty bucket.\n"
                + "Requires the '-b' parameter to specify bucket name.\n";
            }
            case PUT_OBJECT: {
                return "This is provided for Amazon S3 compatibility.\n"
                + "Spectra Logic recommends using Spectra S3 requests to create a PUT job.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "Requires the '-o' parameter to specify local object name.\n"
                + "Optional '-p' parameter (unless | ) to specify prefix or directory name.\n"
                + "Optional '--sync' flag to put only newer or non-extant files.\n"
                + "Optional '-nt' parameter to specify number of threads.\n"
                + "Optional '--ignore-errors' flag to continue on errors.\n"
                + "Optional '--follow-symlinks' flag to follow symlink (default is disregard).\n"
                + "Optional '--metadata' parameter to add metadata (key:value,key2:value2).\n";
            }
            case DELETE_BUCKET: {
                return "Deletes an empty bucket.\n"
                + "Requires the '-b' parameter to specify bucket (by name or UUID).\n"
                + "Use the '--force' flag to delete a bucket and all its contents.\n"
                + "\nUse the get_service command to retrieve a list of buckets.";
            }
            case DELETE_OBJECT: {
                return "Permanently deletes an object.\n"
                + "Requires the '-b' parameter to specify bucketname.\n"
                + "Requires the '-i' parameter to specify object name (UUID or name).\n"
                + "\nUse the get_service command to retrieve a list of buckets."
                + "\nUse the get_bucket command to retrieve a list of objects.";
            }
            case GET_BULK: {
                return "Retrieve multiple objects from a bucket.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "Optional '-d' parameter to specify restore directory (default '.').\n"
                + "Optional '-p' parameter to specify prefix or directory name.\n"
                + "Optional '--sync' flag to retrieve only newer or non-extant files.\n"
                + "Optional '-nt' parameter to specify number of threads.\n";
            }
            case VERIFY_BULK_JOB: {
                return "A verify job reads data from the permanent data store and verifies that the CRC of the data \n"
                + "read matches the expected CRC. Verify jobs ALWAYS read from the data store - even if the data \n"
                + "currently resides in cache.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "Requires the '-o' parameter to specify object (name or UUID).\n"
                + "Optional '-p' parameter to specify prefix or directory name.\n";
            }
            case PUT_BULK: {
                return "Put multiple objects from a directory or pipe into a bucket.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "Requires the '-d' parameter (unless | ) to specify source directory.\n"
                + "Optional '-p' parameter (unless | ) to specify prefix or directory name.\n"
                + "Optional '--sync' flag to put only newer or non-extant files.\n"
                + "Optional '-nt' parameter to specify number of threads.\n"
                + "Optional '--ignore-errors' flag to continue on errors.\n"
                + "Optional '--follow-symlinks' flag to follow symlink (default is disregard).\n";
            }
            case DELETE_TAPE_DRIVE: {
                return "Deletes the specified offline tape drive.\n"
                + "This request is useful when a tape drive is permanently removed from a partition.\n"
                + "Requires the '-i' parameter to specify tape drive ID.\n"
                + "\nUse the get_tape_drives command to retrieve a list of tapes.";
            }
            case DELETE_TAPE_PARTITION: {
                return "Deletes the specified offline tape partition from the BlackPearl gateway configuration.\n"
                + "Any tapes in the partition that have data on them are disassociated from the partition.\n"
                + "Any tapes without data on them and all tape drives associated with the partition are deleted \n"
                + "from the BlackPearl gateway configuration. This request is useful if the partition should never \n"
                + "have been associated with the BlackPearl gateway or if the partition was deleted from the library.\n"
                + "Requires the '-i' parameter to specify tape partition.\n";
            }
            case GET_JOBS: {
                return "Retrieves a list of all current jobs.";
            }
            case GET_JOB: {
                return "Retrieves information about a current job.\n"
                + "Requires the '-i' parameter with the UUID of the job\n"
                + "\nUse the get_jobs command to retrieve a list of jobs.";
            }
            case DELETE_JOB: {
                return "Terminates and removes a current job.\n"
                + "Requires the '-i' parameter with the UUID of the job\n"
                + "Use the '--force' flag to remove objects already loaded into cache.\n"
                + "\nUse the get_jobs command to retrieve a list of jobs.";
            }
            case SYSTEM_INFORMATION: {
                return "Retrieves basic system information: software version, build, and system serial number.\n"
                + "Useful to test communication\n";
            }
            case HEAD_OBJECT: {
                return "Returns metadata but does not retrieves an object from a bucket.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "Requires the '-o' parameter to specify object (name or UUID).\n"
                + "Useful to determine if an object exists and you have permission to access it.";
            }
            case DELETE_FOLDER: {
                return "Deletes a folder and all its contents.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID)."
                + "Requires the '-d' parameter to specify folder name.";
            }
            case PUT_JOB: {
                return "Modify the priority or the start date of a job that is in process.\n"
                + "This also resets the heartbeat for the job, so that it does not timeout.\n"
                + "Requires the '-i' parameter with the UUID of the job.\n"
                + "Optional '--priority' parameter:\n"
                + "    CRITICAL | URGENT | HIGH | NORMAL | LOW | BACKGROUND\n";
            }
            case GET_TAPES: {
                return "Returns a list of all tapes.";
            }
            case DELETE_TAPE: {
                return "Deletes the specified tape which has been permanently lost from the BlackPearl database.\n"
                + "Any data lost as a result is marked degraded to trigger a rebuild.\n"
                + "Requires the '-i' parameter to specify tape ID (UUID or barcode).\n"
                + "\nUse the get_tapes command to retrieve a list of tapes.";
            }
            case PERFORMANCE: {
                return "For internal testing.\n"
                + "Generates mock file streams for put, and a discard (/dev/null)\n"
                + "stream for get. Useful for testing network and system performance.\n"
                + "Requires the '-b' parameter with a unique bucketname to be used for the test.\n"
                + "Requires the '-n' parameter with the number of files to be used for the test.\n"
                + "Requires the '-s' parameter with the size of each file in MB for the test.\n"
                + "Optional '-bs' parameter with the buffer size in bytes (default 1MB).\n"
                + "Optional '-nt' parameter with the number of threads.\n";
            }
            case GET_PHYSICAL_PLACEMENT: {
                return "Returns the location of a single object on tape.\n"
                + "Requires the '-b' parameter to specify bucket (name or UUID).\n"
                + "Requires the '-o' parameter to specify object (name or UUID).\n"
                + "\nUse the get_service command to retrieve a list of buckets."
                + "\nUse the get_bucket command to retrieve a list of objects.";
            }
            case GET_TAPE_FAILURE: {
                return"Returns a list of tape failures.";
            }
            case DELETE_TAPE_FAILURE: {
                return "Deletes a tape failure from the failure list.\n"
                + "Requires the '-i' parameter to specify tape failure ID (UUID).\n"
                + "\nUse the get_tape_failure command to retreive a list of IDs";
            }
            case GET_SERVICE: {
                return "Returns a list of buckets on the device.";
            }
            case GET_DATA_POLICY: {
                return "Returns information about the specified data policy.\n"
                + "Requires the '-i' parameter to specify data policy (UUID or name).\n"
                + "\nUse the get_data_policies command to retrieve a list of policies";
            }
            case GET_DATA_POLICIES: {
                return "Returns information about the specified data policy..\n"
                + "Requires the '-i' parameter to specify data policy (UUID or name)."
                + "\nUse the get_data_policies command to retrieve a list of policies";
            }
            case MODIFY_DATA_POLICY: {
                return "Alter parameters for the specified data policy..\n"
                + "Requires the '-i' parameter to specify data policy (UUID or name).\n"
                + "Requires the '--modify-params' parameter to be set.\n"
                + "Use key:value pair key:value,key2:value2:... Legal values:\n"
                + "    name, checksum_type, default_blob_size, default_get_job_priority, \n"
                + "    default_put_job_priority, default_verify_job_priority, rebuild_priority, \n"
                + "    end_to_end_crc_required, versioning.\n"
                + "See API documentation for possible values).\n"
                + "\nUse the get_data_policies command to retreive a list of policies and current values.";
            }
            case GET_USER: {
                return "Returns information about an individeual user.\n"
                + "Requires the '-i' parameter to specify user (name or UUID).\n"
                + "\nUse the get_users command to retreive a list of users";
            }
            case GET_USERS: {
                return "Returns a list of all users.";
            }
            case MODIFY_USER: {
                 return "Alters information about an individeual user.\n"
                 + "Requires the '-i' parameter to specify user (name or UUID).\n"
                 + "Requires the '--modify-params' parameter to be set.\n"
                 + "Use key:value pair key:value,key2:value2:... Legal values:\n"
                 + "    default_data_policy_id\n"
                 + "\nUse the get_users command to retreive a list of users";
            }
            case GET_OBJECTS_ON_TAPE: {
                return "Returns a list of the contents of a single tape.\n"
                + "Requires the '-i' parameter to specify tape (barcode or UUID).\n"
                + "\nUse the get_tapes command to retreive a list of tapes";
            }
            default: {
                return "No help info for command: " + command.toString();
            }


        }


    }

}

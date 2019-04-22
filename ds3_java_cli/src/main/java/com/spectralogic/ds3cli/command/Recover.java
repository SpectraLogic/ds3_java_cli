/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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
 */package com.spectralogic.ds3cli.command;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.BulkJobType;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.models.RecoveryJob;
import com.spectralogic.ds3cli.util.CliUtils;
import com.spectralogic.ds3cli.util.RecoveryFileManager;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.nio.file.Path;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public class Recover extends CliCommand<DefaultResult> {

    public static final Option RECOVER = Option.builder().
            longOpt("recover")
            .desc("Initiate recovery for bulk put or bulk get jobs. Else it will list files")
            .hasArg(false)
            .build();

    public static final Option DELETE_FILES = Option.builder().
            longOpt("delete")
            .desc("Deletes all recovery files matching argument")
            .hasArg(false)
            .build();

    public static final Option RECOVERY_TYPE  = Option.builder().
            longOpt("job-type")
            .desc("Selects only job type. Possible values: [" + CliUtils.printEnumOptions(BulkJobType.values()) + "]")
            .hasArg(true)
            .build();

    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(RECOVER, DELETE_FILES, RECOVERY_TYPE, ID, BUCKET, FORCE );

    private boolean listOnly;
    private boolean deleteFiles;
    private String bucketName;
    private String id;
    private BulkJobType jobType;

    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, optionalArgs, args);

        this.bucketName = args.getBucket();
        this.id = args.getId();
        this.listOnly = !args.optionExists(RECOVER.getLongOpt()) && !args.optionExists(DELETE_FILES.getLongOpt());
        if (!Guard.isStringNullOrEmpty(args.getOptionValue(RECOVERY_TYPE.getLongOpt()))) {
            this.jobType = BulkJobType.valueOf(args.getOptionValue(RECOVERY_TYPE.getLongOpt()));
        }
        this.deleteFiles = args.optionExists(DELETE_FILES.getLongOpt());
        // require force to run wide opem
        if (this.deleteFiles
                && Guard.isStringNullOrEmpty(this.bucketName)
                && Guard.isStringNullOrEmpty(this.id)
                && this.jobType == null
                && !args.isForce()) {
                    throw new BadArgumentException("--" +DELETE_FILES.getLongOpt() + " requires -"
                            + ID.getOpt() + ", -" + BUCKET.getOpt() + ", -" + RECOVERY_TYPE.getLongOpt()
                            + " or --" + FORCE.getLongOpt());
        }
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        try {
            if (listOnly) {
                return new DefaultResult(RecoveryFileManager.printSearchFiles(this.id, this.bucketName, this.jobType));
            }
            if (deleteFiles) {
                return new DefaultResult(RecoveryFileManager.deleteFiles(this.id, this.bucketName, this.jobType));
            }
            //  get exactly file to recover
            final Iterable<Path> files = RecoveryFileManager.searchFiles(this.id, this.bucketName, this.jobType);
            if (Iterables.isEmpty(files)) {
                return new DefaultResult("No matching recovery files found.");
            }
            if (Iterables.size(files) > 1) {
                return new DefaultResult("Multiple matching recovery files found:\n"
                        + RecoveryFileManager.printSearchFiles(this.id, this.bucketName, this.jobType)
                        + "Please restrict search criteria.");
            }
            final Path file = files.iterator().next();
            final RecoveryJob job = RecoveryFileManager.getRecoveryJobByFile(file.toFile());
            return new DefaultResult(recover(job));
        } catch (final IOException e) {
            throw new CommandException("Recovery Job failed", e);
        }
    }

    private String recover(final RecoveryJob job) throws Exception {
        final CliCommand command = CliCommandFactory.getCommandExecutor(job.getRecoveryCommand()).withProvider(getProvider(), getFileSystemProvider());
        ((RecoverableCommand)command).init(job);
        return command.render().getMessage();
    }

}


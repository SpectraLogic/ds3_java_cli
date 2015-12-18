/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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

import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.command.*;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.views.cli.CommandExceptionCliView;
import com.spectralogic.ds3cli.views.json.CommandExceptionJsonView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Ds3Cli implements Callable<CommandResponse> {

    private final Map<ViewType, Map<CommandValue, View>> views;
    private final Arguments args;
    private final Ds3Provider ds3Provider;
    private final FileUtils fileUtils;

    public Ds3Cli(final Ds3Provider provider, final Arguments args, final FileUtils fileUtils) {
        this.args = args;
        this.ds3Provider = provider;
        this.fileUtils = fileUtils;
        this.views = getViews();
    }

    private Map<ViewType, Map<CommandValue, View>>  getViews() {
        final Map<ViewType, Map<CommandValue, View>>  allViews = new HashMap<>();
        allViews.put( ViewType.CLI, getCliViews() );
        allViews.put( ViewType.JSON, getJsonViews() );
        //TODO XML
        return allViews;
    }

    private Map<CommandValue, View> getCliViews() {
        final com.spectralogic.ds3cli.views.cli.DeleteView deleteView = new com.spectralogic.ds3cli.views.cli.DeleteView();

        final Map<CommandValue, View> cliViews = new HashMap<>();
        cliViews.put(CommandValue.GET_SERVICE,           new com.spectralogic.ds3cli.views.cli.GetServiceView() );
        cliViews.put(CommandValue.GET_BUCKET,            new com.spectralogic.ds3cli.views.cli.GetBucketView() );
        cliViews.put(CommandValue.GET_OBJECT,            new com.spectralogic.ds3cli.views.cli.GetObjectView() );
        cliViews.put(CommandValue.DELETE_BUCKET,         deleteView);
        cliViews.put(CommandValue.DELETE_OBJECT,         deleteView);
        cliViews.put(CommandValue.GET_BULK,              new com.spectralogic.ds3cli.views.cli.GetBulkView() );
        cliViews.put(CommandValue.PUT_BUCKET,            new com.spectralogic.ds3cli.views.cli.PutBucketView() );
        cliViews.put(CommandValue.PUT_BULK,              new com.spectralogic.ds3cli.views.cli.PutBulkView() );
        cliViews.put(CommandValue.PUT_OBJECT,            new com.spectralogic.ds3cli.views.cli.PutObjectView() );
        cliViews.put(CommandValue.DELETE_TAPE_PARTITION, deleteView);
        cliViews.put(CommandValue.DELETE_TAPE_DRIVE,     deleteView);
        cliViews.put(CommandValue.GET_JOBS,              new com.spectralogic.ds3cli.views.cli.GetJobsView());
        cliViews.put(CommandValue.DELETE_JOB, deleteView);
        cliViews.put(CommandValue.SYSTEM_INFORMATION,    new com.spectralogic.ds3cli.views.cli.SystemInformationView());
        cliViews.put(CommandValue.HEAD_OBJECT,           new com.spectralogic.ds3cli.views.cli.HeadObjectView());
        cliViews.put(CommandValue.GET_JOB,               new com.spectralogic.ds3cli.views.cli.GetJobView());
        cliViews.put(CommandValue.DELETE_FOLDER,         deleteView);
        cliViews.put(CommandValue.PUT_JOB,               new com.spectralogic.ds3cli.views.cli.PutJobView());
        cliViews.put(CommandValue.GET_TAPES,             new com.spectralogic.ds3cli.views.cli.GetTapesView());
        cliViews.put(CommandValue.DELETE_TAPE,           deleteView);
        return cliViews;
    }

    private Map<CommandValue, View> getJsonViews() {
        final com.spectralogic.ds3cli.views.json.DeleteView deleteView = new com.spectralogic.ds3cli.views.json.DeleteView();
        final Map<CommandValue, View> jsonViews = new HashMap<>();
        jsonViews.put(CommandValue.GET_SERVICE,           new com.spectralogic.ds3cli.views.json.GetServiceView() );
        jsonViews.put(CommandValue.GET_BUCKET,            new com.spectralogic.ds3cli.views.json.GetBucketView() );
        jsonViews.put(CommandValue.GET_BULK,              new com.spectralogic.ds3cli.views.json.GetBulkView() );
        jsonViews.put(CommandValue.GET_OBJECT,            new com.spectralogic.ds3cli.views.json.GetObjectView() );
        jsonViews.put(CommandValue.DELETE_BUCKET,         deleteView);
        jsonViews.put(CommandValue.DELETE_OBJECT,         deleteView);
        jsonViews.put(CommandValue.PUT_BUCKET,            new com.spectralogic.ds3cli.views.json.PutBucketView() );
        jsonViews.put(CommandValue.PUT_BULK,              new com.spectralogic.ds3cli.views.json.PutBulkView() );
        jsonViews.put(CommandValue.PUT_OBJECT,            new com.spectralogic.ds3cli.views.json.PutObjectView() );
        jsonViews.put(CommandValue.DELETE_TAPE_PARTITION, deleteView);
        jsonViews.put(CommandValue.DELETE_TAPE_DRIVE,     deleteView);
        jsonViews.put(CommandValue.GET_JOBS,              new com.spectralogic.ds3cli.views.json.GetJobsView());
        jsonViews.put(CommandValue.DELETE_JOB,            deleteView);
        jsonViews.put(CommandValue.SYSTEM_INFORMATION,    new com.spectralogic.ds3cli.views.json.SystemInformationView());
        jsonViews.put(CommandValue.HEAD_OBJECT,           new com.spectralogic.ds3cli.views.json.HeadObjectView());
        jsonViews.put(CommandValue.GET_JOB,               new com.spectralogic.ds3cli.views.json.GetJobView());
        jsonViews.put(CommandValue.DELETE_FOLDER,         deleteView);
        jsonViews.put(CommandValue.PUT_JOB,               new com.spectralogic.ds3cli.views.json.PutJobView());
        jsonViews.put(CommandValue.GET_TAPES,             new com.spectralogic.ds3cli.views.json.GetTapesView());
        jsonViews.put(CommandValue.DELETE_TAPE,           deleteView);
        return jsonViews;
    }


    @Override
    public CommandResponse call() throws Exception {
        final CliCommand command = getCommandExecutor();

        final View view = views.get(this.args.getOutputFormat()).get(this.args.getCommand());

        try {
            final String message = view.render(command.init(this.args).call());
            return new CommandResponse(message, 0);
        }
        catch(final CommandException e) {
            final String message;
            if (this.args.getOutputFormat() == ViewType.JSON) {
                message = new CommandExceptionJsonView().render(e);
            }
            else {
                message = new CommandExceptionCliView().render(e);
            }
            return new CommandResponse(message, 1);
        }
    }

    private CliCommand getCommandExecutor() {
        final CommandValue command = this.args.getCommand();
        switch(command) {
            case GET_OBJECT: {
                return new GetObject(this.ds3Provider, this.fileUtils);
            }
            case GET_BUCKET: {
                return new GetBucket(this.ds3Provider, this.fileUtils);
            }
            case PUT_BUCKET: {
                return new PutBucket(this.ds3Provider, this.fileUtils);
            }
            case PUT_OBJECT: {
                return new PutObject(this.ds3Provider, this.fileUtils);
            }
            case DELETE_BUCKET: {
                return new DeleteBucket(this.ds3Provider, this.fileUtils);
            }
            case DELETE_OBJECT: {
                return new DeleteObject(this.ds3Provider, this.fileUtils);
            }
            case GET_BULK: {
                return new GetBulk(this.ds3Provider, this.fileUtils);
            }
            case PUT_BULK: {
                return new PutBulk(this.ds3Provider, this.fileUtils);
            }
            case DELETE_TAPE_DRIVE: {
                return new DeleteTapeDrive(this.ds3Provider, this.fileUtils);
            }
            case DELETE_TAPE_PARTITION: {
                return new DeleteTapePartition(this.ds3Provider, this.fileUtils);
            }
            case GET_JOBS: {
                return new GetJobs(this.ds3Provider, this.fileUtils);
            }
            case GET_JOB: {
                return new GetJob(this.ds3Provider, this.fileUtils);
            }
            case DELETE_JOB: {
                return new DeleteJob(this.ds3Provider, this.fileUtils);
            }
            case SYSTEM_INFORMATION: {
                return new SystemInformation(this.ds3Provider, this.fileUtils);
            }
            case HEAD_OBJECT: {
                return new HeadObject(this.ds3Provider, this.fileUtils);
            }
            case DELETE_FOLDER: {
                return new DeleteFolder(this.ds3Provider, this.fileUtils);
            }
            case PUT_JOB: {
                return new PutJob(this.ds3Provider, this.fileUtils);
            }
            case GET_TAPES: {
                return new GetTapes(this.ds3Provider, this.fileUtils);
            }
            case DELETE_TAPE: {
                return new DeleteJob(this.ds3Provider, this.fileUtils);
            }
            case GET_SERVICE:
            default: {
                return new GetService(this.ds3Provider, this.fileUtils);
            }
        }
    }
}

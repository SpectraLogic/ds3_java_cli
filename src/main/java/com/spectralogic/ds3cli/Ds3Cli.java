package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.command.*;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.views.cli.CommandExceptionCliView;
import com.spectralogic.ds3cli.views.json.CommandExceptionJsonView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Ds3Cli implements Callable<String> {

    private final Map<ViewType, Map<CommandValue, View>> views;
    private final Arguments args;
    private final Ds3Provider ds3Provider;
    private final FileUtils fileUtils;

    Ds3Cli(final Ds3Provider provider, final Arguments args, final FileUtils fileUtils) {
        this.args = args;
        this.ds3Provider = provider;
        this.fileUtils = fileUtils;
        this.views = getViews();
    }

    private Map<ViewType, Map<CommandValue, View>>  getViews(){
        final Map<ViewType, Map<CommandValue, View>>  allViews = new HashMap<>();
        allViews.put( ViewType.CLI, getCliViews() );
        allViews.put( ViewType.JSON, getJsonViews() );
        //TODO XML
        return allViews;
    }

    private Map<CommandValue, View> getCliViews(){
        final Map<CommandValue, View> cliViews = new HashMap<>();
        cliViews.put( CommandValue.GET_SERVICE,     new com.spectralogic.ds3cli.views.cli.GetServiceView() );
        cliViews.put( CommandValue.GET_BUCKET,      new com.spectralogic.ds3cli.views.cli.GetBucketView() );
        cliViews.put( CommandValue.GET_OBJECT,      new com.spectralogic.ds3cli.views.cli.GetObjectView() );
        cliViews.put( CommandValue.DELETE_BUCKET,   new com.spectralogic.ds3cli.views.cli.DeleteBucketView() );
        cliViews.put( CommandValue.DELETE_OBJECT,   new com.spectralogic.ds3cli.views.cli.DeleteObjectView() );
        cliViews.put( CommandValue.GET_BULK,        new com.spectralogic.ds3cli.views.cli.GetBulkView() );
        cliViews.put( CommandValue.PUT_BUCKET,      new com.spectralogic.ds3cli.views.cli.PutBucketView() );
        cliViews.put( CommandValue.PUT_BULK,        new com.spectralogic.ds3cli.views.cli.PutBulkView() );
        cliViews.put( CommandValue.PUT_OBJECT,      new com.spectralogic.ds3cli.views.cli.PutObjectView() );
        return cliViews;
    }

    private Map<CommandValue, View> getJsonViews(){
        final Map<CommandValue, View> jsonViews = new HashMap<>();
        jsonViews.put( CommandValue.GET_SERVICE,    new com.spectralogic.ds3cli.views.json.GetServiceView() );
        jsonViews.put( CommandValue.GET_BUCKET,     new com.spectralogic.ds3cli.views.json.GetBucketView() );
        jsonViews.put( CommandValue.GET_BULK,       new com.spectralogic.ds3cli.views.json.GetBulkView() );
        jsonViews.put( CommandValue.GET_OBJECT,     new com.spectralogic.ds3cli.views.json.GetObjectView() );
        jsonViews.put( CommandValue.DELETE_BUCKET,  new com.spectralogic.ds3cli.views.json.DeleteBucketView() );
        jsonViews.put( CommandValue.DELETE_OBJECT,  new com.spectralogic.ds3cli.views.json.DeleteObjectView() );
        jsonViews.put( CommandValue.PUT_BUCKET,     new com.spectralogic.ds3cli.views.json.PutBucketView() );
        jsonViews.put( CommandValue.PUT_BULK,       new com.spectralogic.ds3cli.views.json.PutBulkView() );
        jsonViews.put( CommandValue.PUT_OBJECT,     new com.spectralogic.ds3cli.views.json.PutObjectView() );
        return jsonViews;
    }


    @Override
    public String call() throws Exception {
        final CliCommand command = getCommandExecutor();

        final View view = views.get(this.args.getOutputFormat()).get(this.args.getCommand());

        try {
            return view.render(command.init(this.args).call());
        }
        catch(final CommandException e) {
            if (this.args.getOutputFormat() == ViewType.JSON) {
                return new CommandExceptionJsonView().render(e);
            }
            else {
                return new CommandExceptionCliView().render(e);
            }
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
            case GET_SERVICE:
            default: {
                return new GetService(this.ds3Provider, this.fileUtils);
            }
        }
    }
}

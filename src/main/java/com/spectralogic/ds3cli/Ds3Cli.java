package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.command.*;
import com.spectralogic.ds3cli.views.cli.*;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.models.Credentials;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Ds3Cli implements Callable<String> {

    private Map<ViewType, Map<CommandValue, View>> views;
    private Arguments args;
    private Ds3Client client;

    Ds3Cli(final Arguments args)  {
        this.args = args;
        this.client = createClient(args);
        this.views = getViews();
    }

    private Map getViews(){
        Map allViews = new HashMap<>();
        allViews.put( ViewType.CLI, getCliViews() );
        allViews.put( ViewType.JSON, getJsonViews() );
        //TODO XML
        return allViews;
    }

    private Map getCliViews(){
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

    // TODO fill in all Commands
    private Map getJsonViews(){
        final Map<CommandValue, View> jsonViews = new HashMap<>();
        jsonViews.put( CommandValue.GET_SERVICE,    new com.spectralogic.ds3cli.views.json.GetServiceView() );
        jsonViews.put( CommandValue.GET_BUCKET,     new com.spectralogic.ds3cli.views.json.GetBucketView() );
        jsonViews.put( CommandValue.DELETE_BUCKET,  new com.spectralogic.ds3cli.views.json.DeleteBucketView() );
        jsonViews.put( CommandValue.DELETE_OBJECT,  new com.spectralogic.ds3cli.views.json.DeleteObjectView() );
        return jsonViews;
    }

    private Ds3Client createClient(final Arguments arguments) {
        final Ds3ClientBuilder builder = Ds3ClientBuilder.create(
                arguments.getEndpoint(),
                new Credentials(arguments.getAccessKey(), arguments.getSecretKey())
        )
                .withHttps(arguments.isHttps())
                .withCertificateVerification(arguments.isCertificateVerification())
                .withRedirectRetries(arguments.getRetries());
        if (arguments.getProxy() != null) {
            builder.withProxy(arguments.getProxy());
        }
        return builder.build();
    }

    @Override
    public String call() throws Exception {
        final CliCommand command = getCommandExecutor();

        final View view = views.get(this.args.getOutputFormat()).get(this.args.getCommand());

        try {
            return view.render(command.init(this.args).call());
        }
        catch(final CommandException e) {
            return e.getMessage();
        }
    }

    private CliCommand getCommandExecutor() {
        final CommandValue command = this.args.getCommand();
        switch(command) {
            case GET_OBJECT: {
                return new GetObject(client);
            }
            case GET_BUCKET: {
                return new GetBucket(client);
            }
            case PUT_BUCKET: {
                return new PutBucket(client);
            }
            case PUT_OBJECT: {
                return new PutObject(client);
            }
            case DELETE_BUCKET: {
                return new DeleteBucket(client);
            }
            case DELETE_OBJECT: {
                return new DeleteObject(client);
            }
            case GET_BULK: {
                return new GetBulk(client);
            }
            case PUT_BULK: {
                return new PutBulk(client);
            }
            case GET_SERVICE:
            default: {
                return new GetService(client);
            }
        }
    }

}

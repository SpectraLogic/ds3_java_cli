package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.command.*;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.models.Credentials;

public class Main implements Runnable {

    private final Arguments args;
    private final Ds3Client client;

    public Main(final Arguments args)  {
        this.args = args;
        this.client = createClient(args);
    }

    private Ds3Client createClient(final Arguments arguments) {
        Ds3Client.Builder builder = Ds3Client
            .builder(
                arguments.getEndpoint(),
                new Credentials(arguments.getAccessKey(), arguments.getSecretKey())
            )
            .withHttpSecure(false);
        if (arguments.getProxy() != null) {
            builder.withProxy(arguments.getProxy());
        }
        return builder.build();
    }

    @Override
    public void run() {
        try {
            System.out.println(getCommandExecutor().init(args).call());
        }
        catch (final Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private CliCommand getCommandExecutor() {
        final CommandValue command = args.getCommand();
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

    public static void main(final String[] args) {
        try {
            final Arguments arguments = new Arguments(args);
            final Main runner = new Main(arguments);
            runner.run();
        }
        catch(final Exception e) {
            System.out.print("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
}

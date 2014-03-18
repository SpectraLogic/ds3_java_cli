package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.GetBucket;
import com.spectralogic.ds3cli.command.GetObject;
import com.spectralogic.ds3cli.command.GetService;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.models.Credentials;

public class Main implements Runnable {

    private final Arguments args;
    private final Ds3Client client;

    public Main(final Arguments args)  {
        this.args = args;
        this.client = createClient(args);
    }

    private Ds3Client createClient(final Arguments arguments) {
        return new Ds3ClientBuilder(arguments.getEndpoint(),
                new Credentials(arguments.getAccessKey(), arguments.getSecretKey())).withHttpSecure(false).build();
    }

    @Override
    public void run() {
        try {
            System.out.println(getCommandExecutor().init(args).call());
        }
        catch (final Exception e) {
            System.out.println("ERROR: " + e.getMessage());
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
            case GET_SERVICE:
            default: {
                return new GetService(client);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final Arguments arguments = new Arguments(args);
        final Main runner = new Main(arguments);
        runner.run();
    }
}

package com.spectralogic.ds3cli;


import com.spectralogic.ds3cli.command.GetService;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.models.Credentials;

public class Main {

    private static Ds3Client createClient(final Arguments arguments) {

        return new Ds3ClientBuilder(arguments.getEndpoint(),
                new Credentials(arguments.getAccessKey(), arguments.getSecretKey())).build();
    }

    public static void main(final String[] args) throws Exception {
        final Arguments arguments = new Arguments(args);
        final CommandValue command = arguments.getCommand();
        final Ds3Client client = createClient(arguments);
        switch(command) {
            case GET_SERVICE: {
                final GetService service = new GetService(client);

                System.out.println(service.call());
                break;
            }
        }
    }
}

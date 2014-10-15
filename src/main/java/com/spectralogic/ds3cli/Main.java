/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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

import com.spectralogic.ds3cli.command.*;
import com.spectralogic.ds3cli.logging.Logging;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.models.Credentials;
import com.spectralogic.ds3client.networking.FailedRequestException;

import java.util.concurrent.Callable;

public class Main implements Callable<String> {

    private final Arguments args;
    private final Ds3Client client;

    public Main(final Arguments args)  {
        this.args = args;
        this.client = createClient(args);
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
        return getCommandExecutor().init(args).call();
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
            System.out.println(runner.call());
        }
        catch(final Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            if (Logging.isVerbose()) {
                e.printStackTrace();
                if (e instanceof FailedRequestException) {
                    Logging.log("Printing out the response from the server:");
                    Logging.log(((FailedRequestException)e).getResponseString());
                }
            }
            System.exit(1);
        }
    }
}

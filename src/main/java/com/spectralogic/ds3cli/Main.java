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

import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Credentials;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        try {
            final Arguments arguments = new Arguments(args);
            final Ds3Client client = createClient(arguments);
            final Ds3Provider provider = new Ds3ProviderImpl(client, Ds3ClientHelpers.wrap(client));
            final FileUtils fileUtils = new FileUtilsImpl();
            final Ds3Cli runner = new Ds3Cli(provider, arguments, fileUtils);
            final CommandResponse response = runner.call();
            System.out.println(response.getMessage());
            System.exit(response.getReturnCode());
        } catch (final Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            if (LOG.isInfoEnabled()) {
                e.printStackTrace();
                if (e instanceof FailedRequestException) {
                    LOG.info("Printing out the response from the server:");
                    LOG.info(((FailedRequestException) e).getResponseString());
                }
            }
            System.exit(2);
        }
    }

    private static Ds3Client createClient(final Arguments arguments) {
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
}
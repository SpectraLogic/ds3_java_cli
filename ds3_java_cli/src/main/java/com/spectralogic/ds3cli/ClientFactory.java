/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
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
 */

package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientBuilder;
import com.spectralogic.ds3client.models.common.Credentials;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.*;

public final class ClientFactory {

    // build client from Arguments
    public static Ds3Client createClient(final Arguments arguments) throws MissingOptionException, BadArgumentException {
        final Ds3ClientBuilder builder = Ds3ClientBuilder.create(
                getEndpoint(arguments),
                new Credentials(getAccessKey(arguments), getSecretKey(arguments))
        )
                .withHttps(arguments.isHttps())
                .withCertificateVerification(arguments.isCertificateVerification())
                .withBufferSize(arguments.getBufferSize())
                .withRedirectRetries(arguments.getRetries());

        if (getProxy(arguments) != null) {
            builder.withProxy(getProxy(arguments));
        }
        return builder.build();
    }

    private static String getEndpoint(final Arguments arguments) throws MissingOptionException {
        return getOptionOrEnv(arguments, ENDPOINT, "DS3_ENDPOINT");
    }

    private static String getAccessKey(final Arguments arguments) throws MissingOptionException {
        return getOptionOrEnv(arguments, ACCESS_KEY, "DS3_ACCESS_KEY");
    }

    private static String getSecretKey(final Arguments arguments) throws MissingOptionException {
        return getOptionOrEnv(arguments, SECRET_KEY, "DS3_SECRET_KEY");
    }

    private static String getProxy(final Arguments arguments) {
        String proxyValue = arguments.getOptionValue(PROXY.getOpt());
        if (Guard.isStringNullOrEmpty(proxyValue)) {
            proxyValue = System.getenv("http_proxy");
        }
        return proxyValue;
    }

    // use argument if provided, else use environment variable, else throw exception
    private static String getOptionOrEnv(final Arguments arguments, final Option option, final String envName)
            throws MissingOptionException {
        String value = arguments.getOptionValue(option.getOpt());
        if (Guard.isStringNullOrEmpty(value)) {
            value = System.getenv(envName);
            if (Guard.isStringNullOrEmpty(value)) {
                throw new MissingOptionException("Missing Endpoint: define " + envName + " or use " + option.getOpt());
            }
        }
        return value;
    }

}




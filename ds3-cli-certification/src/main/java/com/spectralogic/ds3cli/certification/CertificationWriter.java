/*
 * ******************************************************************************
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
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.certification;

import ch.qos.logback.classic.Logger;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.Ds3ProviderImpl;
import com.spectralogic.ds3cli.FileSystemProviderImpl;
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.CliCommandFactory;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileSystemProvider;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetUserSpectraS3Request;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;


public class CertificationWriter {

    private static final String EXT = ".html";
    private static final Logger LOG =  (Logger) LoggerFactory.getLogger(CertificationWriter.class);

    private OutputStreamWriter OUT;
    private long currentTimeTag = 0L;

    public CertificationWriter(final String title) throws IOException {
        OUT = new OutputStreamWriter(new FileOutputStream(title + EXT), "UTF-8");
        writeHeader(title);
    }

    public  void writeHeader(final String title) throws IOException {
        LOG.info("NEW FILE: {}", title);
        OUT.write(String.format("<html><head><title>%s</title></head>\n", title));
        OUT.write(String.format("<body class=\"testbody\"><h1 class=\"maintitle\">%s</h1>\n", title));
    }

    public  void startNewTest(final String testTitle ) throws IOException {
        currentTimeTag = new Date().getTime();
        LOG.info("-----------------\nSTART TEST: {}", testTitle);
        OUT.write(String.format("<a name=\"%d\" />\n", currentTimeTag));
        OUT.write(String.format("<h2 class=\"testtitle\">%s</h2>\n", testTitle));
    }

    public  void insertLog(final String message ) throws IOException {
        LOG.info(message);
        OUT.write(String.format("<p class=\"logentry\">%s</p>\n", message));
    }

    public  void insertCommand(final String message ) throws IOException {
        LOG.info(message);
        OUT.write(String.format("<p class=\"commandline\"><strong>%s</strong></p>\n", message));
    }

    public void insertPreformat(final String message ) throws IOException {
        LOG.info(message);
        OUT.write(String.format("<pre>%s</pre>\n", message));
    }

    public void insertCommandOutput(final String title, final String output) throws IOException {
        insertLog(title);
        insertPreformat(output);
    }

    public  void finishTest(final String testTitle, final boolean success)  throws IOException {
        LOG.info("COMPLETE TEST: {} ({})\n-----------------", testTitle, (success ? "Passed" : "Failed"));
        final String link = String.format("<a href=\"#%d\">Start of test</a>\n", currentTimeTag);
        OUT.write(String.format("<p class=\"endtest\"><strong>%s (%s)</strong> %s</p>\n", testTitle, (success ? "Passed" : "Failed"), link));
    }

    /**
     * // same as com.spectralogic.ds3cli.helpers.Util.command() but takes command line as string
     *
     */
    public  CommandResponse runCommand(final Ds3Client client, final String commandLine ) throws Exception {
        insertCommand("Run command: " + commandLine);
        final Arguments args = new Arguments(commandLine.split(" "));
        final Ds3Provider provider = new Ds3ProviderImpl(client, Ds3ClientHelpers.wrap(client));
        final FileSystemProvider fileSystemProvider = new FileSystemProviderImpl();
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(provider, fileSystemProvider);
        command.init(args);
        final CommandResponse response = command.render();
        insertLog("Command output:");
        insertPreformat(response.getMessage());
        return response;
    }

    public void close() throws IOException {
        OUT.write("</body></html>");
        OUT.flush();
        OUT.close();
    }
}

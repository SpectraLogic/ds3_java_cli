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
import com.spectralogic.ds3client.Ds3Client;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;

import static com.spectralogic.ds3cli.helpers.Util.command;


public class CertificationWriter {

    private static final String EXT = ".html";
    private static final Logger LOG =  (Logger) LoggerFactory.getLogger(CertificationWriter.class);

    private final OutputStreamWriter OUT;
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
     * Run ds3-cli-helpers::Util.command with a cmd line string
     */
    public  CommandResponse runCommand(final Ds3Client client, final String commandLine ) throws Exception {
        insertCommand("Run command: " + commandLine);
        final Arguments args = new Arguments(commandLine.split(" "));
        final CommandResponse response = command(client, args);
        insertLog("Command output: (" + (new Date().getTime() - currentTimeTag)/1000 + "sec)");
        insertPreformat(response.getMessage());
        return response;
    }

    public void close() throws IOException {
        OUT.write("</body></html>");
        OUT.flush();
        OUT.close();
    }
}

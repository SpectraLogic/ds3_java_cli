/*
 * ******************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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

import com.spectralogic.ds3cli.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.ResourceBundle;


public class CertificationWriter implements Closeable {

    private static final String EXT = ".html";
    private static final Logger LOG = LoggerFactory.getLogger(CertificationWriter.class);

    private final OutputStreamWriter OUT;
    private long currentTimeTag = 0L;

    public CertificationWriter(final String fileName) throws IOException {
        OUT = new OutputStreamWriter(new FileOutputStream(fileName + EXT), "UTF-8");
        LOG.info("NEW FILE: {}", fileName);
    }

    public void writeHeader() throws IOException {
        final ResourceBundle bundle = ResourceBundle.getBundle("com.spectralogic.ds3cli.certification.writer");
        final String css = bundle.getString("CSS");
        final String pageTitle = bundle.getString("TITLE") + " " + Constants.DATE_FORMAT.format(new Date());
        OUT.write(String.format("<html><head><title>%s</title>\n<style type=\"text/css\">%s</style>\n</head>\n", pageTitle, css));
        OUT.write(String.format("<body class=\"testbody\"><h1 class=\"maintitle\">%s</h1>\n", pageTitle));
    }

    public void startNewTest(final String testTitle) throws IOException {
        currentTimeTag = new Date().getTime();
        LOG.info("-----------------\nSTART TEST: {}", testTitle);
        OUT.write(String.format("<a name=\"%d\" />\n", currentTimeTag));
        OUT.write(String.format("<h2 class=\"testtitle\">%s</h2>\n", testTitle));
    }

    public void insertLog(final String message) throws IOException {
        LOG.info(message);
        OUT.write(String.format("<p class=\"logentry\">%s</p>\n", message));
    }

    public void insertPreformat(final String message) throws IOException {
        LOG.info(message);
        OUT.write(String.format("<pre>%s</pre>\n", message));
    }

    public void insertCommand(final String command, final String responseOutput) throws IOException {
        LOG.info("{}: {}", command, responseOutput);
        OUT.write(String.format("<p class=\"commandline\"><strong>Run command: %s</strong></p>\n", command));
        insertLog("Command output: (" + (new Date().getTime() - currentTimeTag)/1000 + "sec)");
        insertPreformat(responseOutput);
    }

    public void insertPerformanceMetrics(
            final long startTime,
            final long curTime,
            final long bytesTransferred,
            final boolean isPutCommand) throws IOException {
        final String messagePrefix = isPutCommand ? "PUT" : "GET";
        final double elapsedTime = (curTime - startTime == 0)? 0.0: (curTime - startTime)/1000D;
        final double megaBytesTransferred = bytesTransferred/1024D/1024D;
        final double mbps = megaBytesTransferred / elapsedTime;

        insertLog(String.format("%s Transferred (%.03f MB), Time (%.03f sec), MB / Sec (%.03f)", messagePrefix, megaBytesTransferred, elapsedTime, mbps));
    }

    public void finishTest(final String testTitle, final boolean success)  throws IOException {
        LOG.info("COMPLETE TEST: {} ({})\n-----------------", testTitle, (success ? "Passed" : "Failed"));
        final String link = String.format("<a href=\"#%d\">Start of test</a>\n", currentTimeTag);
        OUT.write(String.format("<p class=\"endtest\"><strong>%s (%s)</strong> %s</p>\n", testTitle, (success ? "Passed" : "Failed"), link));
    }

    public void close() throws IOException {
        OUT.write("</body></html>");
        OUT.flush();
        OUT.close();
    }
}

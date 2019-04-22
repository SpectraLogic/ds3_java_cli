/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3client.helpers.DataTransferredListener;
import com.spectralogic.ds3client.helpers.ObjectCompletedListener;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class PerformanceListener implements DataTransferredListener, ObjectCompletedListener {
    private final long startTime;
    private final int totalNumberOfFiles;
    private final long numberOfMB;
    private final boolean isPutCommand;
    private long totalByteTransferred = 0;
    private int numberOfFiles = 0;
    private double highestMbps = 0.0;
    private double time;
    private long content;
    private double mbps;
    private final OutputStreamWriter osw;


    public PerformanceListener(
            final long startTime,
            final int totalNumberOfFiles,
            final long numberOfMB,
            final boolean isPutCommand) {
        this(startTime, totalNumberOfFiles, numberOfMB, isPutCommand, null);
    }

    public PerformanceListener(
            final long startTime,
            final int totalNumberOfFiles,
            final long numberOfMB,
            final boolean isPutCommand,
            final OutputStreamWriter osw) {
        this.startTime = startTime;
        this.totalNumberOfFiles = totalNumberOfFiles;
        this.numberOfMB = numberOfMB;
        this.isPutCommand = isPutCommand;
        this.osw = osw;
    }

    @Override
    public void dataTransferred(final long size) {

        final long currentTime = System.currentTimeMillis();
        synchronized (this) {
            totalByteTransferred += size;
            time = currentTime - this.startTime == 0 ? 1.0 : (currentTime - this.startTime) / 1000D;
            content = totalByteTransferred / 1024L / 1024L;
            mbps = content / time;
            if (mbps > highestMbps) highestMbps = mbps;
        }
        printStatistics();
    }

    @Override
    public void objectCompleted(final String s) {
        synchronized (this) {
            numberOfFiles += 1;
        }
        printStatistics();
    }

    private void printStatistics() {
        final String messagePrefix;
        if (isPutCommand) {
            messagePrefix = "Putting";
        }
        else {
            messagePrefix = "Getting";
        }

        final String statsMsg = String.format("\r%s Statistics: (%d/%d MB), files (%d/%d completed), Time (%.03f sec), MBps (%.03f), Highest MBps (%.03f)",
                    messagePrefix, content, numberOfMB, numberOfFiles, totalNumberOfFiles, time, mbps, highestMbps);

        if (osw == null) {
            System.out.print(statsMsg);
        } else {
            try {
                osw.write(statsMsg);
            } catch (final IOException ioe) {
                System.out.println("PerformanceListener caught IOException attempting to write to OutputStreamWriter: " + ioe.getMessage());
                System.out.print(ioe.getStackTrace().toString());
                //throw ioe; // Requires signature change
            }
        }
    }
}

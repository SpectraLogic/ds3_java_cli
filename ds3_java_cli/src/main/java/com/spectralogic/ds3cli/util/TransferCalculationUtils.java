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

package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3client.models.bulk.Ds3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferCalculationUtils {
    private final static Logger LOG = LoggerFactory.getLogger(TransferCalculationUtils.class);
    public static void logTransferSpeed(final long totalTime, final long dataTransferred) {
        final long bytesPerMilli = dataTransferred/totalTime;
        final long mbPerMilli = bytesPerMilli/1024/1024;
        final long mbPerSec = mbPerMilli * 1000;
        LOG.info("Aggregate transfer speed: %d MB/s", mbPerSec);
    }

    public static long sum(final Iterable<Ds3Object> objects) {
        long sum = 0;
        for(final Ds3Object object : objects) {
            sum += object.getSize();
        }
        return sum;
    }
}

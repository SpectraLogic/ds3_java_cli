package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3cli.logging.Logging;
import com.spectralogic.ds3client.models.bulk.Ds3Object;

public class TransferCalculationUtils {
    public static void logTransferSpeed(final long totalTime, final long dataTransfered) {
        final long bytesPerMilli = dataTransfered/totalTime;
        final long mbPerMilli = bytesPerMilli/1024/1024;
        final long mbPerSec = mbPerMilli * 1000;
        Logging.logf("Aggregate transfer speed: %d MB/s", mbPerSec);
    }

    public static long sum(final Iterable<Ds3Object> objects) {
        long sum = 0;
        for(final Ds3Object object : objects) {
            sum += object.getSize();
        }
        return sum;
    }
}

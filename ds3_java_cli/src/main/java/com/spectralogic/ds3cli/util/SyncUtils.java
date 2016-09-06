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

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Request;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.models.Contents;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.regex.Pattern;

public final class SyncUtils {

    private final static Logger LOG = LoggerFactory.getLogger(SyncUtils.class);
    private final static int VERSION_SUPPORTED = 3;
    private final static int MAJOR_INDEX = 0;

    public static boolean isSyncSupported(final Ds3Client client) throws IOException {
        final String buildInfo = client.getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request()).getSystemInformationResult().getBuildInformation().getVersion();
        final String[] buildInfoArr = buildInfo.split((Pattern.quote(".")));
        if (Integer.parseInt(buildInfoArr[MAJOR_INDEX]) < VERSION_SUPPORTED) {
            LOG.info("The SYNC COMMAND can not be used with BlackPearl {}", buildInfo);
            return false;
        }
        LOG.info("Using BlackPearl {}", buildInfo);
        return true;
    }

    public static boolean isNewFile(final Path localFile, final Contents serverFile, final boolean isPutCommand) throws IOException {
        return isNewFileHelper(Files.getLastModifiedTime(localFile), serverFile.getLastModified(), isPutCommand);
    }

    private static boolean isNewFileHelper(final FileTime localFileLastModifiedTime, final Date serverFileLastModifiedTime, final boolean isPutCommand) {

        final DateTime localFileDateTime = new DateTime(localFileLastModifiedTime.toMillis());
        final DateTime serverFileDateTime = new DateTime(serverFileLastModifiedTime);

        if (isPutCommand) {
            return DateTimeComparator.getInstance().compare(localFileDateTime, serverFileDateTime) > 0;
        }
        return DateTimeComparator.getInstance().compare(localFileDateTime, serverFileDateTime) < 0;
    }

    public static boolean needToSync(final Ds3ClientHelpers helpers, final String bucketName, final Path filePath, final String ds3ObjName, final boolean isPutCommand) throws IOException, XmlProcessingException {
        final Iterable<Contents> objects = helpers.listObjects(bucketName);
        for (final Contents obj : objects) {
            if (ds3ObjName.equals(obj.getKey())) {
                if (SyncUtils.isNewFile(filePath, obj, isPutCommand)) {
                    LOG.info("Syncing new version of {}",ds3ObjName);
                    return true;
                } else {
                    LOG.info("No need to SYNC {}", ds3ObjName);
                    return false;
                }
            }
        }
        return true;
    }
}

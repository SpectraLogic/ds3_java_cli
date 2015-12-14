/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetSystemInformationRequest;

import com.spectralogic.ds3client.models.Contents;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SyncUtils {

    private final static Logger LOG = LoggerFactory.getLogger(SyncUtils.class);
    private final static int VersionSupported = 3;
    private final static int MajorIndex = 0;

    public static boolean IsSyncSupported(final Ds3Client client) throws IOException, SignatureException {
        final String buildInfo = client.getSystemInformation(new GetSystemInformationRequest()).getSystemInformation().getBuildInformation().getVersion();
        final String[] buildInfoArr = buildInfo.split((Pattern.quote(".")));
        if (Integer.parseInt(buildInfoArr[MajorIndex]) < VersionSupported) {
            LOG.info("The sync command can not be used with BlackPearl " +  buildInfo);
            return false;
        }
        LOG.info("Using BlackPearl " +  buildInfo);
        return true;
    }

    public static boolean NeedToSync(final Path localFile, final Contents serverFile, final boolean isPutCommand) throws IOException {
        return NeedToSyncHelper(Files.getLastModifiedTime(localFile).toString(), serverFile.getLastModified(), isPutCommand);
    }

    private static boolean NeedToSyncHelper(final String localFileLastModifiedTime, final String serverFileLastModifiedTime, final boolean isPutCommand) {

        final DateTime localFileDateTime = new DateTime(localFileLastModifiedTime);
        final DateTime serverFileDateTime = new DateTime(serverFileLastModifiedTime);

        if (isPutCommand) {
            return DateTimeComparator.getInstance().compare(localFileDateTime, serverFileDateTime) > 0;
        }
        return DateTimeComparator.getInstance().compare(localFileDateTime, serverFileDateTime) < 0;
    }

    public static Iterable<Path> listObjectsForDirectory(final Path directory) throws IOException {
        final List<Path> objects = new ArrayList<>();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                objects.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        return objects;
    }

    public static String GetFileName(final Path rootDir, final Path filePath) {
        return rootDir.relativize(filePath).toString().replace("\\", "/");
    }

    public static long GetFileSize(final Path path) throws IOException {
        return Files.size(path);
    }

    public static boolean FileExists(final Path filePath) {
        return Files.exists(filePath);
    }
}

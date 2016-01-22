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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.GetSystemInformationRequest;
import com.spectralogic.ds3cli.command.PutBulk;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SignatureException;
import java.util.Map;
import java.util.regex.Pattern;

import static com.spectralogic.ds3cli.command.PutBulk.*;

public final class Utils {

    private final static Logger LOG = LoggerFactory.getLogger(Utils.class);
    public final static double MINIMUM_VERSION_SUPPORTED = 1.2;

    public static boolean isCliSupported(final Ds3Client client) throws IOException, SignatureException {
        final String buildInfo = client.getSystemInformation(new GetSystemInformationRequest()).getSystemInformation().getBuildInformation().getVersion();
        final String[] buildInfoArr = buildInfo.split((Pattern.quote(".")));
        final double version = Double.valueOf(
                String.format("%s.%s", buildInfoArr[0], buildInfoArr[1]));

        return version >= MINIMUM_VERSION_SUPPORTED;

    }

    public static ImmutableList<Path> listObjectsForDirectory(final Path directory) throws IOException {
        final ImmutableList.Builder<Path> objectsBuilder = ImmutableList.builder();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                objectsBuilder.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        return objectsBuilder.build();
    }

    public static String getFileName(final Path rootDir, final Path filePath) {
        return rootDir.relativize(filePath).toString().replace("\\", "/");
    }

    public static long getFileSize(final Path path) throws IOException {
        return Files.size(path);
    }

    public static boolean fileExists(final Path filePath) {
        return Files.exists(filePath);
    }

    public static String nullGuard(final String message) {
        if (message == null) {
            return "N/A";
        }

        return message;
    }

    public static ObjectsForDirectory getObjectsForDirectory(final Iterable<Path> filteredObjects, final Path inputDirectory, final boolean ignoreErrors) throws IOException {
        final ImmutableList.Builder<Ds3Object> objectsBuilder = ImmutableList.builder();
        final ImmutableList.Builder<PutBulk.IgnoreFile> ignoredBuilder = ImmutableList.builder();

        for (final Path path : filteredObjects) {
            try {
                objectsBuilder.add(new Ds3Object(
                        Utils.getFileName(inputDirectory, path),
                        Utils.getFileSize(path)));
            } catch (final IOException ex) {
                if (!ignoreErrors) throw ex;
                LOG.warn(String.format("WARN: file '%s' has an error and will be ignored", path.getFileName()));
                ignoredBuilder.add(new PutBulk.IgnoreFile(path, ex.toString()));
            }
        }

        return new ObjectsForDirectory(objectsBuilder.build(), ignoredBuilder.build());
    }

    public static ObjectsForDirectory getObjectsForDirectory(final Path inputDirectory, final boolean ignoreErrors) throws IOException {
        final Iterable<Path> localFiles = Utils.listObjectsForDirectory(inputDirectory);
        return getObjectsForDirectory(localFiles, inputDirectory, ignoreErrors);
    }

    public static ObjectsForDirectory getObjectsForDirectory(final ImmutableMap<String, String> pipedFiles, final boolean ignoreErrors) throws IOException {
        final ImmutableList.Builder<Ds3Object> objectsBuilder = ImmutableList.builder();
        final ImmutableList.Builder<PutBulk.IgnoreFile> ignoredBuilder = ImmutableList.builder();

        for (final Map.Entry<String, String> entry : pipedFiles.entrySet()) {
            try {
                objectsBuilder.add(new Ds3Object(
                        entry.getKey(),
                        Utils.getFileSize(Paths.get(entry.getValue()))));
            } catch (final IOException ex) {
                if (!ignoreErrors) throw ex;
                LOG.warn(String.format("WARN: file '%s' has an error and will be ignored", entry.getValue()));
                ignoredBuilder.add(new PutBulk.IgnoreFile(Paths.get(entry.getValue()), ex.toString()));
            }
        }

        return new ObjectsForDirectory(objectsBuilder.build(), ignoredBuilder.build());
    }

    /***
     *
     * @param objectName
     * @return
     */
    public static String normalizeObjectName(final String objectName) {

        final String path;
        final int colonIndex = objectName.indexOf(':');
        if (colonIndex != -1) {
            path = objectName.substring(colonIndex + 2);
        } else if (objectName.startsWith("/")) {
            return objectName.substring(1);
        } else if (objectName.startsWith("./")) {
            return objectName.substring(2);
        } else {
            path = objectName;
        }
        if (!path.contains("\\")) {
            return path;
        }

        return path.replace("\\", "/");
    }

}

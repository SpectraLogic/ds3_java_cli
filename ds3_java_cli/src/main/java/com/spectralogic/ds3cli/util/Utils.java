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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3cli.command.PutBulk;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Request;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.utils.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.spectralogic.ds3cli.command.PutBulk.ObjectsToPut;

public final class Utils {

    private final static Logger LOG = LoggerFactory.getLogger(Utils.class);

    public final static boolean isWindows = System.getProperty("os.name").contains("Windows");
    public final static double MINIMUM_VERSION_SUPPORTED = 1.2;


    public static boolean isCliSupported(final Ds3Client client) throws IOException, SignatureException {
        final String buildInfo = client.getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request()).getSystemInformationResult().getBuildInformation().getVersion();
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
        if (rootDir == null) {
            return normalizeObjectName(filePath.toString());
        }
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

    public static String nullGuardToString(final Object o) {
        // default alternate
        return nullGuardToString(o, "N/A");
    }

    public static String nullGuardToString(final Object o, final String alternate) {
        if (o == null) {
            return alternate;
        }
        return o.toString();
    }

    public static String nullGuardToDate(final Object o, SimpleDateFormat dateFormat) {
        if (o == null) {
            return dateFormat.format(new Date(0L));
        }
        return dateFormat.format(o);
    }

    public static ObjectsToPut getObjectsToPut(final Iterable<Path> filteredObjects, final Path inputDirectory, final boolean ignoreErrors) throws IOException {
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

        return new ObjectsToPut(objectsBuilder.build(), ignoredBuilder.build());
    }

    public static String normalizeObjectName(final String objectName) {
        if (isWindows) {
            return windowsNormalizeObjectName(objectName);
        }

        return unixNormalizeObjectName(objectName);
    }

    /**
     * Normalizes the object name to remove windows path from beginning
     * of object name
     */
    protected static String windowsNormalizeObjectName(final String objectName) {
        final String path;

        final int colonIndex = objectName.indexOf(':');
        if (colonIndex != -1) {
            path = objectName.substring(colonIndex + 2);
        } else if (objectName.startsWith("\\")) {
            path = objectName.substring(1);
        } else if (objectName.startsWith(".\\")) {
            path = objectName.substring(2);
        } else if (objectName.startsWith("..\\")) {
            path = removePrefixRecursively(objectName, "..\\");
        } else {
            path = objectName;
        }

        return path.replace("\\", "/");
    }

    /**
     * Recursively removes the specified prefix from the beginning of the
     * object name
     */
    protected static String removePrefixRecursively(final String objectName, final String prefix) {
        if (Guard.isStringNullOrEmpty(objectName)) {
            return "";
        }
        if (Guard.isStringNullOrEmpty(prefix) || !objectName.startsWith(prefix)) {
            return objectName;
        }
        return removePrefixRecursively(objectName.substring(prefix.length()), prefix);
    }

    /**
     * Normalizes the object name to remove linux path from beginning
     * of object name
     */
    protected static String unixNormalizeObjectName(final String objectName) {
        final String path;

        if (objectName.startsWith("/")) {
            path = objectName.substring(1);
        } else if (objectName.startsWith("./")) {
            path = objectName.substring(2);
        } else if (objectName.startsWith("../")) {
            path = removePrefixRecursively(objectName, "../");
        } else {
            path = objectName;
        }

        return path;
    }

    public static ImmutableList<Path> getPipedFilesFromStdin(final FileUtils fileUtils) throws IOException {
        final ImmutableList.Builder<Path> pipedFiles = new ImmutableList.Builder<>();
        final InputStream inputStream = System.in;
        final int availableBytes = inputStream.available();
        if (availableBytes > 0) {
            // Wrap the System.in inside BufferedReader
            // But do not close it in a finally block, as we
            // did not open System.in; enforcing the rule that
            // he who opens it, closes it; leave the closing to the OS.
            final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            LOG.info("Piped files are:");
            String line;
            while ((line = in.readLine()) != null) {
                final Path file = Paths.get(line);
                if (!fileUtils.isRegularFile(file) && !Files.isSymbolicLink(file)) {
                    LOG.warn(String.format("WARN: piped data must be a regular/symbolic link file and not a directory ==> %s will be skipped", line));
                    continue;
                }
                LOG.info("File \"" + file.toString() + "\" from stdin");
                pipedFiles.add(file);
            }
        }

        return pipedFiles.build();
    }

    public static boolean isPipe() throws IOException {
        return System.in.available() > 0;
    }

    public static Map<String, String> getMetadataValues(final Path path) {
        try {
            final FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            return ImmutableMap.of(Constants.DS3_LAST_MODIFIED, Long.toString(lastModifiedTime.toMillis()));
        } catch (final IOException e) {
            LOG.error("Could not get the last modified time for file: " + path, e);
            return null;
        }
    }

    public static void restoreLastModified(final String filename, final com.spectralogic.ds3client.networking.Metadata metadata, final Path path) {
        if (metadata.keys().contains(Constants.DS3_LAST_MODIFIED)) {
            try {
                final long lastModifiedMs = Long.parseLong(metadata.get(Constants.DS3_LAST_MODIFIED).get(0));
                final FileTime lastModified = FileTime.from(lastModifiedMs, TimeUnit.MILLISECONDS);
                Files.setLastModifiedTime(path, lastModified);
            } catch (final Throwable t) {
                LOG.error("Failed to restore the last modified date for object: " + filename, t);
            }

        } else {
            LOG.warn("Object (" + filename + ") does not contain a last modified field");
        }
    }

    public static boolean isCollectionNullOrEmpty(ImmutableMap collection) {
        return (collection == null) || collection.isEmpty();
    }

    public static boolean isCollectionNullOrEmpty(ImmutableCollection collection) {
        return (collection == null) || collection.isEmpty();
    }

}

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

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.command.PutBulk;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class FileUtils {

    private final static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public final static boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");

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

    public static PutBulk.ObjectsToPut getObjectsToPut(final Iterable<Path> filteredObjects, final Path inputDirectory, final boolean ignoreErrors) throws IOException {
        final ImmutableList.Builder<Ds3Object> objectsBuilder = ImmutableList.builder();
        final ImmutableList.Builder<PutBulk.IgnoreFile> ignoredBuilder = ImmutableList.builder();

        for (final Path path : filteredObjects) {
            try {
                objectsBuilder.add(new Ds3Object(
                        getFileName(inputDirectory, path),
                        getFileSize(path)));
            } catch (final IOException ex) {
                if (!ignoreErrors) {
                    throw ex;
                }
                LOG.warn(String.format("WARN: file '%s' has an error and will be ignored", path.getFileName()));
                ignoredBuilder.add(new PutBulk.IgnoreFile(path, ex.toString()));
            }
        }

        return new PutBulk.ObjectsToPut(objectsBuilder.build(), ignoredBuilder.build());
    }

    public static String normalizeObjectName(final String objectName) {
        if (IS_WINDOWS) {
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
        if (com.spectralogic.ds3client.utils.Guard.isStringNullOrEmpty(objectName)) {
            return "";
        }
        if (com.spectralogic.ds3client.utils.Guard.isStringNullOrEmpty(prefix) || !objectName.startsWith(prefix)) {
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

    public static ImmutableList<Path> getPipedFilesFromStdin(final FileSystemProvider fileSystemProvider) throws IOException {
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
                if (!fileSystemProvider.isRegularFile(file) && !Files.isSymbolicLink(file)) {
                    LOG.warn(String.format("WARN: piped data must be a regular/symbolic link file and not a directory ==> %s will be skipped", line));
                    continue;
                }
                LOG.info("File \"{}\" from stdin", file.toString());
                pipedFiles.add(file);
            }
        }

        return pipedFiles.build();
    }

    /**
     * parse a string to get time span in seconds
     * @param diff format ddays.hhours.mminutes.sseconds e.g., "d2.h8.m30.s45"
     * @return span in seconds
     */
    public static long dateDiffToSeconds(final String diff) {
        long secs = 0;
        final String[] units = diff.split("[.]");
        for (final String unit : units) {
            if (unit.matches("[dhms][0-9]+")) {
                final char unitdesc = unit.charAt(0);
                switch (unitdesc) {
                    case 's':
                        secs += Integer.parseInt(unit.replace("s", ""));
                        break;
                    case 'm':
                        secs += Integer.parseInt(unit.replace("m", "")) * 60;
                        break;
                    case 'h':
                        secs += Integer.parseInt(unit.replace("h", "")) * 60 * 60;
                        break;
                    case 'd':
                        secs += Integer.parseInt(unit.replace("d", "")) * 60 * 60 * 24;
                        break;
                }
            }
        }
        return secs;
    }

    /**
     * parse a string to get absolute date
     * @param diff format Yyear.Mmonth.Ddate.hhours.mminutes.sseconds e.g., "Y2016.M11.D10.h12"
     * @return Date
     */
    public static Date parseParamDate(final String diff) throws java.text.ParseException {
        final String dateString =  "0000-01-01T00:00:00.000UTC";
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
        final char[] dateChars =  dateString.toCharArray();

        String timeZone = "UTC";
        final String[] units = diff.split("[.]");
        for (final String unit : units) {
            if (unit.matches("[YMDhms][0-9]+") || unit.matches("[Z]...")) {
                final char unitdesc = unit.charAt(0);
                switch (unitdesc) {
                    case 's':
                        // pad to at least two chars and replace two chars at dateChars[19] and left
                        updateDateArray(dateChars, unit.replace("s", "00"), 19, 2);
                        break;
                    case 'm':
                        updateDateArray(dateChars, unit.replace("m", "00"), 16, 2);
                        break;
                    case 'h':
                        updateDateArray(dateChars, unit.replace("h", "00"), 13, 2);
                        break;
                    case 'D':
                        updateDateArray(dateChars, unit.replace("D", "00"), 10, 2);
                        break;
                    case 'M':
                        updateDateArray(dateChars, unit.replace("M", "00"), 7, 2);
                        break;
                    case 'Y':
                        updateDateArray(dateChars, unit.replace("Y", "0000"), 4, 4);
                        break;
                    case 'Z':
                        updateDateArray(dateChars, unit.replace("Z", ""), 26, 3);
                        timeZone = unit.replace("Z", "");
                        break;
                }
            } else {
                throw new ParseException("Cannot process date token: '" + unit + "'", diff.indexOf(unit));
            }
        }
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return dateFormat.parse(new String(dateChars));
    }

    private static void updateDateArray(final char[] chars, final String value, final int rightPos, final int fieldWidth) {
        // value is a padded string copy the chars we need from the right
        for (int i = 1; i <= fieldWidth; i++ ) {
            chars[rightPos - i] = value.charAt(value.length() - i);
        }
    }
}

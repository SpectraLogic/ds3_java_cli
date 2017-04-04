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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.spectralogic.ds3client.utils.Guard.isStringNullOrEmpty;

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

    public static FileUtils.ObjectsToPut getObjectsToPut(final Iterable<Path> filteredObjects, final Path inputDirectory, final boolean ignoreErrors) throws IOException {
        final ImmutableList.Builder<Ds3Object> objectsBuilder = ImmutableList.builder();
        final ImmutableList.Builder<FileUtils.IgnoreFile> ignoredBuilder = ImmutableList.builder();

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
                ignoredBuilder.add(new FileUtils.IgnoreFile(path, ex.toString()));
            }
        }

        return new FileUtils.ObjectsToPut(objectsBuilder.build(), ignoredBuilder.build());
    }

    /**
     * Perform platform-specific normalization of path names on a list of Paths
     * used by bulk get
     * @param pipedFiles
     * @return normalized pathname mapped to filename
     */
    public static ImmutableMap<String, String> getNormalizedObjectNames(final ImmutableList<Path> pipedFiles) {
        final ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (final Path file : pipedFiles) {
            map.put(normalizeObjectName(file.toString()), file.toString());
        }
        return map.build();
    }

    /**
     * Perform platform-specific normalization of path names as strings
     * use by bulk put
     * @param pipedFiles
     * @return normalized pathname mapped to path
     */
    public static ImmutableMap<String, String> normalizedObjectNames(final ImmutableList<String> pipedFiles) {
        final ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (final String fileName : pipedFiles) {
            map.put(normalizeObjectName(fileName), fileName);
        }
        return map.build();
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
        if (isStringNullOrEmpty(objectName)) {
            return "";
        }
        if (isStringNullOrEmpty(prefix) || !objectName.startsWith(prefix)) {
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

    public static ImmutableList<String> getPipedListFromStdin(final FileSystemProvider fileSystemProvider) throws IOException {
        final ImmutableList.Builder<String> pipedNames = new ImmutableList.Builder<>();
        final InputStream inputStream = System.in;
        final int availableBytes = inputStream.available();
        if (availableBytes > 0) {
            // Wrap the System.in inside BufferedReader
            // But do not close it in a finally block, as we
            // did not open System.in; enforcing the rule that
            // he who opens it, closes it; leave the closing to the OS.
            final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            LOG.info("Piped list is:");
            String line;
            while ((line = in.readLine()) != null) {
                LOG.info("Name \"{}\" from stdin", line);
                pipedNames.add(line);
            }
        }
        return pipedNames.build();
    }

    public static ImmutableList<Path> getPipedFilesFromStdin(final FileSystemProvider fileSystemProvider) throws IOException {
        final ImmutableList<String> pipedNames = getPipedListFromStdin(fileSystemProvider);
        final ImmutableList.Builder<Path> pipedFiles = new ImmutableList.Builder<>();
        for (final String name : pipedNames) {
            final Path file = Paths.get(name);
            if (!fileSystemProvider.isRegularFile(file) && !Files.isSymbolicLink(file)) {
                LOG.warn(String.format("WARN: piped data must be a regular/symbolic link file and not a directory ==> %s will be skipped", file));
                continue;
            }
            LOG.info("File \"{}\" from stdin", file.toString());
            pipedFiles.add(file);
        }
        return pipedFiles.build();
    }

    public static void appendPrefix(final Iterable<Ds3Object> ds3Objects, final String prefix) {
        if (isStringNullOrEmpty(prefix)) {
            for (final Ds3Object obj : ds3Objects) {
                obj.setName(prefix + obj.getName());
            }
        }
    }

    public static class ObjectsToPut {

        private final ImmutableList<Ds3Object> ds3Objects;
        private final ImmutableList<FileUtils.IgnoreFile> ds3IgnoredObjects;

        public ObjectsToPut(final ImmutableList<Ds3Object> ds3Objects, final ImmutableList<FileUtils.IgnoreFile> ds3IgnoredObjects) {
            this.ds3Objects = ds3Objects;
            this.ds3IgnoredObjects = ds3IgnoredObjects;
        }

        public ImmutableList<Ds3Object> getDs3Objects() {
            return this.ds3Objects;
        }

        public ImmutableList<FileUtils.IgnoreFile> getDs3IgnoredObjects() {
            return this.ds3IgnoredObjects;
        }
    }

    public static class IgnoreFile {
        @JsonProperty("path")
        private final String path;

        @JsonProperty("error_message")
        private final String errorMessage;

        public IgnoreFile(final Path path, final String errorMessage) {
            this.path = path.toString();
            this.errorMessage = errorMessage;
        }

        public String getPath() {
            return this.path;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
    }


}

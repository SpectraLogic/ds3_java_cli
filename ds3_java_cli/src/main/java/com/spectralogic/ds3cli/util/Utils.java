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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class Utils {

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
        final String normalizedPath = path.replace("\\", "/");
        return normalizedPath;
    }
}

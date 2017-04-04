/*
 * *****************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.models.BulkJobType;
import com.spectralogic.ds3cli.models.RecoveryJob;
import com.spectralogic.ds3client.utils.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

public class RecoveryFileManager {

    public static String GET_PREFIX = "ds3Get_";
    public static String PUT_PREFIX = "ds3Put_";
    public static String DIR_PREFIX = "ds3";
    public static String RECOVERY_FILE_EXTENSION = ".json";

    private static final Path tempdir;
    static {
        if (!Guard.isStringNullOrEmpty(System.getProperty("java.io.tmpdir"))) {
            tempdir = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + DIR_PREFIX);
        } else {
            tempdir = Paths.get("." + File.separator + DIR_PREFIX);
        }
    }

    private final static Logger LOG = LoggerFactory.getLogger(RecoveryFileManager.class);

    private static void ensureDirExists() throws IOException {
        if (!Files.isDirectory(tempdir)) {
            Files.createDirectory(tempdir);
        }
    }

    public static File createGetFile(final String id) throws IOException {
        ensureDirExists();
        return new File(tempdir.toFile(), GET_PREFIX + id + RECOVERY_FILE_EXTENSION);
    }
    public static File createPutFile(final String id) throws IOException {
        ensureDirExists();
        return new File(tempdir.toFile(), PUT_PREFIX + id + RECOVERY_FILE_EXTENSION);
    }

    public static String getFileList() {
        try {
            return printFileList(searchFiles(null,null,null));
        } catch (final IOException e) {
            return e.getMessage();
        }
    }

    public static String printFileList(final Iterable<Path> files) {
        if (Iterables.isEmpty(files)) {
            return "No matching recovery files found.";
        }
        final StringBuffer fileList = new StringBuffer();
        try {
            for (Path file : files) {
                final RecoveryJob job = getRecoveryJobByName(file.getFileName().toString());
                fileList.append(job.toString());
                fileList.append("\n");
            }
            return fileList.toString();
        } catch (final IOException e) {
            return e.getMessage();
        }
    }

    public static String deleteFileList(final Iterable<Path> files) {
        if (Iterables.isEmpty(files)) {
            return "No matching recovery files found.";
        }
        final StringBuffer fileList = new StringBuffer("Deleted:\n");
        try {
            for (Path file : files) {
                final RecoveryJob job = getRecoveryJobByName(file.getFileName().toString());
                Files.delete(file);
                fileList.append(job.toString());
                fileList.append("\n");
            }
            return fileList.toString();
        } catch (final IOException e) {
            return e.getMessage();
        }
    }

    public static File getFileById(final String id) throws IOException {
        ensureDirExists();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempdir, "*" + id + RECOVERY_FILE_EXTENSION)) {
            final Iterator<Path> iterator = stream.iterator();
            if (iterator.hasNext()) {
                return iterator.next().toFile();
            }
            throw new IOException("File not found matching ID: " + id);
        } catch (final IOException e) {
            throw new IOException("Could not open file for ID:" + id, e);
        }
    }

    public static String printSearchFiles(final String id, final String bucketName, final BulkJobType type) throws IOException {
        return printFileList(searchFiles(id, bucketName, type));
    }

    public static String deleteFiles(final String id, final String bucketName, final BulkJobType type) throws IOException {
        return deleteFileList(searchFiles(id, bucketName, type));
    }

    public static Iterable<Path> searchFiles(final String id, final String bucketName, final BulkJobType type) throws IOException {
        ensureDirExists();
        List<Path> matches = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempdir, "*" + RECOVERY_FILE_EXTENSION)) {
            for (final Path file : stream) {
                final RecoveryJob job = getRecoveryJobByFile(file.toFile());
                // match all supplied params
                if(id == null || job.getId().compareTo(UUID.fromString(id)) == 0) {
                    if(Guard.isStringNullOrEmpty(bucketName) || job.getBucketName().equals(bucketName)) {
                        if(type == null || job.getJobType().equals(type)) {
                            matches.add(file);
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new IOException("Could not search files." , e);
        }
        return matches;
    }

    public static RecoveryJob getRecoveryJobByName(final String fileName) throws IOException {
        return getRecoveryJobByFile( new File(tempdir.toFile(), fileName));
    }

    public static RecoveryJob getRecoveryJobByFile(final File jobFile) throws IOException {
        final byte[] json = com.google.common.io.Files.toByteArray(jobFile);
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RecoveryJob.class);
    }

    public static boolean writeRecoveryJob(final RecoveryJob job) {
        try {
            final File file;
            if(job.getJobType() == BulkJobType.GET_BULK) {
                file = createGetFile(job.getId().toString());
            } else {
                file = createPutFile(job.getId().toString());
            }
            try (BufferedWriter writer = com.google.common.io.Files.newWriter(file, Charset.forName("utf-8"))) {
                writer.write(JsonMapper.toJson(job));
            } catch (final Exception inner) {
                throw inner;
            }
            return true;
        } catch (final IOException e) {
            LOG.error("Could not create recovery file", e);
            return false;
        }
    }

    public static void deleteRecoveryCommand(final UUID jobId) {
        try {
            deleteFiles(jobId.toString(), null, null);
        } catch (final IOException e) {
            LOG.error("Could not delete recovery file in temporary space.", e);
        }
    }

}

package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtilsImpl implements FileUtils {
    @Override
    public boolean exists(final Path path) {
        return Files.exists(path);
    }

    @Override
    public boolean isRegularFile(final Path path) {
        return Files.isRegularFile(path);
    }

    @Override
    public long size(final Path path) throws IOException {
        return Files.size(path);
    }

    @Override
    public void createDirectories(final Path path) throws IOException {
        Files.createDirectories(path);
    }
}

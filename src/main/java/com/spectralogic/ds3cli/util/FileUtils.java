package com.spectralogic.ds3cli.util;

import java.io.IOException;
import java.nio.file.Path;

public interface FileUtils {
    boolean exists(final Path path);
    boolean isRegularFile(final Path path);
    long size(final Path path) throws IOException;
    void createDirectories(final Path path) throws IOException;
}

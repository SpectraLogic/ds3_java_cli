package com.spectralogic.ds3cli.util;

import com.spectralogic.ds3cli.command.PutBulk;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectPutter;
import com.spectralogic.ds3client.helpers.MetadataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.Map;

public class PrefixedFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder, MetadataAccess {

    private final static Logger LOG = LoggerFactory.getLogger(PutBulk.class);


    final private LoggingFileObjectPutter objectPutter;
    final private String prefix;
    final private Path inputDirectory;

    public PrefixedFileObjectPutter(final Path inputDirectory, final String prefix) {
        this.objectPutter = new LoggingFileObjectPutter(inputDirectory);
        this.prefix = prefix;
        this.inputDirectory = inputDirectory;
    }

    @Override
    public SeekableByteChannel buildChannel(final String fileName) throws IOException {
        final String objectName = removePrefix(fileName);
        return this.objectPutter.buildChannel(objectName);
    }

    @Override
    public Map<String, String> getMetadataValue(final String fileName) {
        final String unPrefixedFile = removePrefix(fileName);

        final Path path = inputDirectory.resolve(unPrefixedFile);
        return MetadataUtils.getMetadataValues(path);
    }

    private String removePrefix(final String fileName) {
        if (this.prefix == null) {
            return fileName;
        } else {
            if (!fileName.startsWith(this.prefix)) {
                LOG.info("The object ({}) does not begin with prefix {}.  Ignoring adding the prefix.", fileName,  this.prefix);
                return fileName;
            } else {
                return fileName.substring(this.prefix.length());
            }
        }
    }

    static class LoggingFileObjectPutter implements Ds3ClientHelpers.ObjectChannelBuilder {
        final private FileObjectPutter objectPutter;

        public LoggingFileObjectPutter(final Path inputDirectory) {
            this.objectPutter = new FileObjectPutter(inputDirectory);
        }

        @Override
        public SeekableByteChannel buildChannel(final String s) throws IOException {
            LOG.info("Putting {} to ds3 endpoint", s);
            return this.objectPutter.buildChannel(s);
        }
    }

}

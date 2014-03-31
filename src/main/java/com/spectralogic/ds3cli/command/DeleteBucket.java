package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.DeleteBucketRequest;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;

public class DeleteBucket extends CliCommand {
    
    private String bucketName;

    public DeleteBucket(Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The delete bucket command requires '-b' to be set.");
        }
        return this;
    }

    @Override
    public String call() throws Exception {
        try {
            getClient().deleteBucket(new DeleteBucketRequest(bucketName));
        }
        catch (final IOException e) {
            return "Error: Request failed with the following error: " + e.getMessage();
        }
        return "Success: Deleted bucket '" + bucketName + "'.";
    }
}

package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.DeleteObjectRequest;
import org.apache.commons.cli.MissingOptionException;

import java.io.IOException;

public class DeleteObject extends CliCommand {
    
    private String bucketName;
    private String objectName;

    public DeleteObject(Ds3Client client) {
        super(client);
    }

    @Override
    public CliCommand init(Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The get object command requires '-b' to be set.");
        }
        objectName = args.getObjectName();
        if (objectName == null) {
            throw new MissingOptionException("The get object command requires '-o' to be set.");
        }
        return this;
    }

    @Override
    public String call() throws Exception {
        try {
            getClient().deleteObject(new DeleteObjectRequest(bucketName, objectName));
        }
        catch (final IOException e) {
            return "Error: Request failed with the following error: " + e.getMessage();
        }
        return "Success: Deleted object '" + bucketName + "'.";
    }
}

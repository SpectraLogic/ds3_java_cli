package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.HeadObjectResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.HeadObjectRequest;
import com.spectralogic.ds3client.commands.HeadObjectResponse;
import org.apache.commons.cli.MissingOptionException;

public class HeadObject extends CliCommand<HeadObjectResult> {

    private String objectName;
    private String bucketName;

    public HeadObject(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
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
    public String getLongHelp() {
        final StringBuffer helpStringBuffer = new StringBuffer();
        helpStringBuffer.append("Returns metadata but does not retrieves an object from a bucket.\n");
        helpStringBuffer.append("Requires the '-b' parameter to specify bucket (name or UUID).\n");
        helpStringBuffer.append("Requires the '-o' parameter to specify object (name or UUID).\n");
        helpStringBuffer.append("Useful to determine if an object exists and you have permission to access it.");

        return helpStringBuffer.toString();
    }


    @Override
    public HeadObjectResult call() throws Exception {
        final HeadObjectResponse result = getClient().headObject(new HeadObjectRequest(bucketName, objectName));
        return new HeadObjectResult(result);
    }
}

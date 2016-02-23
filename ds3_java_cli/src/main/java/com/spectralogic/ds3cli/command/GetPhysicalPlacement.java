package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.models.GetPhysicalPlacementWithFullDetailsResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.commands.spectrads3.GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import org.apache.commons.cli.MissingOptionException;

import java.util.Arrays;
import java.util.List;


public class GetPhysicalPlacement extends CliCommand<GetPhysicalPlacementWithFullDetailsResult> {

    private String bucketName;
    private String objectName;

    public GetPhysicalPlacement(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        super(ds3Provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        this.bucketName = args.getBucket();
        if (this.bucketName == null) {
            throw new MissingOptionException("The get physical placement command requires '-b' to be set.");
        }

        this.objectName = args.getObjectName();
        if (this.objectName == null) {
            throw new MissingOptionException("The get physical placement command requires '-o' to be set.");
        }

        return this;
    }

    @Override
    public GetPhysicalPlacementWithFullDetailsResult call() throws Exception {
        try {
            final List<Ds3Object> objectsList = Arrays.asList(new Ds3Object(objectName));

            final GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Response response = getClient().
                    getPhysicalPlacementForObjectsWithFullDetailsSpectraS3(
                            new GetPhysicalPlacementForObjectsWithFullDetailsSpectraS3Request(bucketName, objectsList));

            return new GetPhysicalPlacementWithFullDetailsResult(response.getBulkObjectListResult());
        } catch (final FailedRequestException e) {
            throw new CommandException("Failed Get Physical Placement", e);
        }
    }
}

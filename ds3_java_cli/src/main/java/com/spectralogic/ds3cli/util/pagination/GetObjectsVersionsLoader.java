package com.spectralogic.ds3cli.util.pagination;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetObjectsDetailsSpectraS3Request;
import com.spectralogic.ds3client.commands.spectrads3.GetObjectsDetailsSpectraS3Response;
import com.spectralogic.ds3client.helpers.pagination.SpectraS3PaginationLoader;
import com.spectralogic.ds3client.models.S3Object;
import com.spectralogic.ds3client.utils.collections.LazyIterable;

public class GetObjectsVersionsLoader implements LazyIterable.LazyLoader<S3Object> {
    private final SpectraS3PaginationLoader<S3Object, GetObjectsDetailsSpectraS3Request, GetObjectsDetailsSpectraS3Response> objectVersionPaginator;

    public GetObjectsVersionsLoader(final Ds3Client client, final String bucketName, final String objectName) {
        objectVersionPaginator = new SpectraS3PaginationLoader<>(new GetObjectsVersionsPaginatingCommand(client, bucketName, objectName), 1000, 5);
    }

    @Override
    public Iterable<S3Object> getNextValues() {
        return objectVersionPaginator.getNextValues();
    }
}

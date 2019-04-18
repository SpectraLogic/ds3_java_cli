package com.spectralogic.ds3cli.util.pagination;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.models.S3Object;
import com.spectralogic.ds3client.utils.collections.LazyIterable;

public class GetObjectVersionsLoaderFactory implements LazyIterable.LazyLoaderFactory<S3Object> {

    private final Ds3Client client;
    private final String bucketName;
    private final String objectName;

    public GetObjectVersionsLoaderFactory(final Ds3Client client, final String bucketName, final String objectName) {
        this.client = client;
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    @Override
    public LazyIterable.LazyLoader<S3Object> create() {
        return new GetObjectsVersionsLoader(client, bucketName, objectName);
    }
}

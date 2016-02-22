package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.BulkObjectList;

public class GetPhysicalPlacementWithFullDetailsResult implements Result {

    private BulkObjectList bulkObjectListResult;

    public GetPhysicalPlacementWithFullDetailsResult(final BulkObjectList bulkObjectList) {
        this.bulkObjectListResult = bulkObjectList;
    }

    public BulkObjectList getPhysicalPlacementWithDetails() {
        return bulkObjectListResult;
    }
}

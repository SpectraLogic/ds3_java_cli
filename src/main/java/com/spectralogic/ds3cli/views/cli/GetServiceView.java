package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3client.models.Bucket;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

import java.util.List;

public class GetServiceView implements View<ListAllMyBucketsResult> {
    @Override
    public String render(final ListAllMyBucketsResult obj) {
        if (obj == null) {
            return "You do not have any buckets";
        }

    return "Owner: " + obj.getOwner().getDisplayName() + "\n" +
        ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(obj));
    }

    private String[][] formatBucketList(final ListAllMyBucketsResult result) {
        final List<Bucket> buckets = result.getBuckets();
        final String [][] formatArray = new String[buckets.size()][];
        for(int i = 0; i < buckets.size(); i ++) {
            final Bucket bucket = buckets.get(i);
            final String [] bucketArray = new String[2];
            bucketArray[0] = bucket.getName();
            bucketArray[1] = bucket.getCreationDate();
            formatArray[i] = bucketArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Bucket Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Creation Date", ASCIITable.ALIGN_RIGHT)
        };
    }
}

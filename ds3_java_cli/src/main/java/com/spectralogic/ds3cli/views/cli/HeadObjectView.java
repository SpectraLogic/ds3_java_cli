package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Joiner;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.HeadObjectResult;
import com.spectralogic.ds3client.networking.Metadata;

import java.util.List;
import java.util.Set;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class HeadObjectView implements View<HeadObjectResult> {
    @Override
    public String render(final HeadObjectResult obj) throws JsonProcessingException {

        switch (obj.getStatus()) {
            case DOESNTEXIST: return "The object does not exist";
            case UNKNOWN: return "Authentication failed";
            default: {
                if (obj.getMetadata().keys().size() == 0) {
                    return "The object exists and does not have any metadata";
                } else {
                    return "The object exists and contains the following metadata:\n"
                            + ASCIITable.getInstance().getTable(getHeaders(), formatMetadata(obj.getMetadata()));
                }
            }
        }
    }

    private String[][] formatMetadata(final Metadata metadata) {
        final Set<String> keys = metadata.keys();
        final String [][] formatArray = new String[keys.size()][];
        int arrayIndex = 0;

        for (final String key : keys) {
            final List<String> values = metadata.get(key);
            final String [] bucketArray = new String[2];
            bucketArray[0] = nullGuard(key);
            bucketArray[1] = nullGuard(Joiner.on(", ").join(values));
            formatArray[arrayIndex] = bucketArray;


            arrayIndex++;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("MetaData Key", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("MetaData Values", ASCIITable.ALIGN_LEFT)
        };
    }
}

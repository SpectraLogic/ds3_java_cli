package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3client.models.bulk.BulkObject;
import com.spectralogic.ds3client.models.bulk.MasterObjectList;
import com.spectralogic.ds3client.models.bulk.Objects;

import java.util.ArrayList;
import java.util.Iterator;

public class GetJobView implements View<GetJobResult> {
    @Override
    public String render(final GetJobResult obj) throws JsonProcessingException {

        final MasterObjectList mol = obj.getJobDetails();

        final String returnString = String.format("JobId: %s | Status: %s | Bucket: %s | Type: %s | Priority: %s | User Name: %s | Creation Date: %s | Total Size: %s | Total Transferred: %d",
                mol.getJobId().toString(), mol.getStatus().toString(), mol.getBucketName(), mol.getRequestType().toString(), mol.getPriority().toString(), mol.getUserName(),
                mol.getStartDate(), mol.getOriginalSizeInBytes(), mol.getCompletedSizeInBytes());


        if (mol.getObjects() == null) {
            return returnString;
        }

        return returnString + "\n" + ASCIITable.getInstance().getTable(getHeaders(), formatJobDetails(obj.getJobDetails().getObjects().iterator()));
    }

    private String[][] formatJobDetails(final Iterator<Objects> iterator) {
        final ArrayList<String[]> contents = new ArrayList<>();

        while(iterator.hasNext()) {
            final Objects chunk = iterator.next();

            for (final BulkObject obj : chunk) {
                final String[] arrayEntry = new String[5];

                arrayEntry[0] = obj.getName();
                arrayEntry[1] = Long.toString(obj.getLength());
                arrayEntry[2] = Boolean.toString(obj.isInCache());
                arrayEntry[3] = Long.toString(chunk.getChunkNumber());
                arrayEntry[4] = chunk.getChunkId().toString();

                contents.add(arrayEntry);
            }
        }

        return contents.toArray(new String[contents.size()][]);
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("File Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Size", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("In Cache", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Chunk Number", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Chunk ID", ASCIITable.ALIGN_RIGHT)};
    }
}

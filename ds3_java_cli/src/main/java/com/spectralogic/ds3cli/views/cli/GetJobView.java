package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.MasterObjectList;
import com.spectralogic.ds3client.models.Objects;

import java.util.ArrayList;
import java.util.Iterator;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class GetJobView implements View<GetJobResult> {
    @Override
    public String render(final GetJobResult obj) throws JsonProcessingException {

        final MasterObjectList mol = obj.getJobDetails();

        final String returnString = String.format("JobId: %s | Status: %s | Bucket: %s | Type: %s | Priority: %s | User Name: %s | Creation Date: %s | Total Size: %d | Total Transferred: %d",
                mol.getJobId().toString(), mol.getStatus().toString(), mol.getBucketName(), mol.getRequestType().toString(), mol.getPriority().toString(), mol.getUserName().toString(),
                mol.getStartDate().toString(), mol.getOriginalSizeInBytes(), mol.getCompletedSizeInBytes());

        if (mol.getObjects() == null || mol.getObjects().isEmpty()) {
            return returnString;
        }

        return returnString + "\n" + ASCIITable.getInstance().getTable(getHeaders(), formatJobDetails(obj.getJobDetails().getObjects().iterator()));
    }

    private String[][] formatJobDetails(final Iterator<Objects> iterator) {
        final ArrayList<String[]> contents = new ArrayList<>();

        while(iterator.hasNext()) {
            final Objects chunk = iterator.next();

            for (final BulkObject obj : chunk.getObjects()) {
                final String[] arrayEntry = new String[5];

                arrayEntry[0] = nullGuard(obj.getName());
                arrayEntry[1] = nullGuard(Long.toString(obj.getLength()));
                arrayEntry[2] = nullGuard(Boolean.toString(obj.getInCache()));
                arrayEntry[3] = nullGuard(Long.toString(chunk.getChunkNumber()));
                arrayEntry[4] = nullGuard(chunk.getChunkId().toString());

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

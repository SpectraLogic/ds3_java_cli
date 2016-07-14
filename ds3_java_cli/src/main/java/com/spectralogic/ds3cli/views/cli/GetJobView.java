package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.MasterObjectList;
import com.spectralogic.ds3client.models.Objects;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimeZone;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetJobView extends TableView<GetJobResult> {

    protected Iterator<Objects> objectsIterator;

    @Override
    public String render(final GetJobResult obj) {

        final MasterObjectList mol = obj.getJobDetails();

        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String returnString = String.format(
                "JobId: %s | Status: %s | Bucket: %s | Type: %s | Priority: %s | User Name: %s | Creation Date: %s | Total Size: %s | Total Transferred: %s",
                nullGuardToString(mol.getJobId()), nullGuardToString(mol.getStatus()), nullGuard(mol.getBucketName()),
                nullGuardToString(mol.getRequestType()), nullGuardToString(mol.getPriority()), nullGuardToString(mol.getUserName()),
                nullGuardToDate(mol.getStartDate(), DATE_FORMAT), nullGuardToString(mol.getOriginalSizeInBytes()),
                nullGuardToString(mol.getCompletedSizeInBytes()));

        if (mol.getObjects() == null || mol.getObjects().isEmpty()) {
            return returnString;
        }
        this.objectsIterator = mol.getObjects().iterator();

        initTable(ImmutableList.of("File Name", "Size", "In Cache", "Chunk Number", "Chunk ID"));

        return returnString + "\n" + ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());
    }

    protected String[][] formatTableContents() {
        final ArrayList<String[]> contents = new ArrayList<>();

        while (this.objectsIterator.hasNext()) {
            final Objects chunk = this.objectsIterator.next();

            for (final BulkObject obj : chunk.getObjects()) {
                final String[] arrayEntry = new String[this.columnCount];
                arrayEntry[0] = nullGuard(obj.getName());
                arrayEntry[1] = nullGuardToString(obj.getLength());
                arrayEntry[2] = nullGuardToString(obj.getInCache());
                arrayEntry[3] = nullGuardToString(chunk.getChunkNumber());
                arrayEntry[4] = nullGuardToString(chunk.getChunkId());
                contents.add(arrayEntry);
            }
        }
        return contents.toArray(new String[contents.size()][]);
    }
}

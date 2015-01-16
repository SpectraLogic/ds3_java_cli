package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.BucketResult;
import com.spectralogic.ds3client.models.Contents;

import java.util.ArrayList;
import java.util.Iterator;

public class GetBucketView implements View<BucketResult> {

    @Override
    public String render(final BucketResult br) {
        if( !br.getObjIterator().hasNext()) {
            return "No objects were reported in the bucket '" + br.getBucketName() + "'";
        }

        return ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(br.getObjIterator()));
    }

    private String[][] formatBucketList(final Iterator<Contents> iterator) {
        final ArrayList<String[]> contents = new ArrayList<>();

        while(iterator.hasNext()) {

            final Contents content = iterator.next();
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = nullGuard(content.getKey());
            arrayEntry[1] = nullGuard(Long.toString(content.getSize()));
            arrayEntry[2] = nullGuard(content.getOwner().getDisplayName());
            arrayEntry[3] = nullGuard(content.getLastModified());
            arrayEntry[4] = nullGuard(content.geteTag());
            contents.add(arrayEntry);
        }

        return contents.toArray(new String[contents.size()][]);
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("File Name", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Size", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Owner", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Last Modified", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("ETag", ASCIITable.ALIGN_RIGHT)};
    }

    private String nullGuard(String message) {
        if(message == null) {
            return "N/A";
        }
        return message;
    }
}

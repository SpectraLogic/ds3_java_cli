package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetTapesResult;
import com.spectralogic.ds3client.models.tape.Tape;
import com.spectralogic.ds3client.models.tape.Tapes;

import java.util.List;

public class GetTapesView implements View<GetTapesResult> {
    @Override
    public String render(final GetTapesResult obj) throws JsonProcessingException {
        final Tapes result = obj.getTapes();
        if( (result == null) || (null == result.getTapes()) ){
            return "You do not have any tapes";
        }

        return ASCIITable.getInstance().getTable(getHeaders(), formatTapeList(result));
    }

    private String[][] formatTapeList(final Tapes result) {
        final List<Tape> tapes = result.getTapes();
        final String [][] formatArray = new String[tapes.size()][];
        for(int i = 0; i < tapes.size(); i ++) {
            final Tape tape = tapes.get(i);
            final String [] bucketArray = new String[5];
            bucketArray[0] = tape.getBarcode();
            bucketArray[1] = tape.getId().toString();
            bucketArray[2] = tape.getState().toString();
            bucketArray[3] = tape.getLastModified();
            bucketArray[4] = Long.toString(tape.getAvailableRawCapacity());
            bucketArray[5] = tape.getMostRecentFailure();
            formatArray[i] = bucketArray;
        }
        return formatArray;
    }

    private ASCIITableHeader[] getHeaders() {
        return new ASCIITableHeader[]{
                new ASCIITableHeader("Bar Code", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("ID", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("State", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Last Modified", ASCIITable.ALIGN_LEFT),
                new ASCIITableHeader("Available Raw Capacity", ASCIITable.ALIGN_RIGHT),
                new ASCIITableHeader("Most Recent Failure", ASCIITable.ALIGN_RIGHT)
        };
    }
}

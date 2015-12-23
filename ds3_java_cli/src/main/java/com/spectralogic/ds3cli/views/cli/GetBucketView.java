/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3client.models.Contents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class GetBucketView implements View<GetBucketResult> {

    @Override
    public String render(final GetBucketResult br) {
        if( (null == br.getObjIterator()) || !br.getObjIterator().hasNext()) {
            return "No objects were reported in bucket '" + br.getBucketName() + "'";
        }

        return ASCIITable.getInstance().getTable(getHeaders(), formatBucketList(br.getObjIterator()));
    }

    private String[][] formatBucketList(final Iterator<Contents> iterator) {
        final List<String[]> contents = new ArrayList<>();

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
}

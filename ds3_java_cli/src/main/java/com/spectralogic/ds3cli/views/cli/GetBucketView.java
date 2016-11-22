/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.views.cli;

import com.bethecoder.ascii_table.ASCIITable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.spectralogic.ds3cli.models.GetBucketResult;
import com.spectralogic.ds3client.models.Contents;

import static com.spectralogic.ds3cli.utils.Constants.DATE_FORMAT;
import static com.spectralogic.ds3cli.utils.Guard.nullGuard;
import static com.spectralogic.ds3cli.utils.Guard.nullGuardFromDate;

public class GetBucketView extends TableView<GetBucketResult> {

    private Iterable<Contents> contents;

    @Override
    public String render(final GetBucketResult br) {
        // Bucket details
        final String bucketDesc = new com.spectralogic.ds3cli.views.cli.GetBucketDetailsView().render(br);

        if (null == br.getResult() || Iterables.isEmpty(br.getResult())) {
            return "No objects were reported in bucket.";
        }
        this.contents = br.getResult();
        initTable(ImmutableList.of("File Name", "Size", "Owner", "Last Modified", "ETag"));
        setTableDataAlignment(ImmutableList.of(ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT, ASCIITable.ALIGN_RIGHT ,ASCIITable.ALIGN_LEFT, ASCIITable.ALIGN_RIGHT ));

        final String bucketContents =  ASCIITable.getInstance().getTable(getHeaders(), formatTableContents());

        return String.format("%s\n%s", bucketDesc, bucketContents);
    }

    protected String[][] formatTableContents() {

        final ImmutableList.Builder<String[]> builder = ImmutableList.builder();

        for (final Contents content : contents) {
            final String[] arrayEntry = new String[5];
            arrayEntry[0] = nullGuard(content.getKey());
            arrayEntry[1] = nullGuard(Long.toString(content.getSize()));
            arrayEntry[2] = nullGuard(content.getOwner().getDisplayName());
            arrayEntry[3] = nullGuardFromDate(content.getLastModified(), DATE_FORMAT);
            arrayEntry[4] = nullGuard(content.getETag());
            builder.add(arrayEntry);
        }

        final ImmutableList<String[]> contentStrings = builder.build();
        return contentStrings.toArray(new String[contentStrings.size()][]);
    }

}

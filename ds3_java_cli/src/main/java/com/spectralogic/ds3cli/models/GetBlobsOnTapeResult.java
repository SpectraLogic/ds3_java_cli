/*
 * ******************************************************************************
 *   Copyright 2014 - 2016 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.models;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.spectralogic.ds3client.models.BulkObject;
import com.spectralogic.ds3client.models.BulkObjectList;
import com.spectralogic.ds3client.models.Contents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static com.spectralogic.ds3cli.util.Utils.nullGuard;

public class GetBlobsOnTapeResult implements Result {
    final private String tapeId;
    final private String barcode;
    final private Iterator<BulkObject> objIterator;

    public String getTapeId() { return this.tapeId; };
    public String getBarcode() { return this.barcode; }
    public Iterator<BulkObject> getObjIterator() {
        return objIterator;
    }

    public GetBlobsOnTapeResult(final String id, final String barcode, final Iterator<BulkObject> objIterator) {
        this.tapeId = id;
        this.barcode = barcode;
        this.objIterator = objIterator;
    }
}

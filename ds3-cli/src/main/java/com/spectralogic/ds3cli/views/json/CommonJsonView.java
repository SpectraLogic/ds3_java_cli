/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CommonJsonView {

    public enum Status {
        OK, ERROR
    }

    private final Map<String, Object> metaData;
    private Object data;
    private Status status;
    private String message;

    public CommonJsonView() {
        this.metaData = new HashMap<>();
    }

    public CommonJsonView addMetaData(final String key, final Object value) {
        this.metaData.put(key, value);
        return this;
    }

    @JsonProperty("Meta")
    public Map<String, Object> getMetaData() {
        return this.metaData;
    }

    public CommonJsonView status(final Status status) {
        this.status = status;
        return this;
    }

    @JsonProperty("Status")
    public Status getStatus() {
        return this.status;
    }

    @JsonProperty("Data")
    public Object getData() {
        return this.data;
    }

    public CommonJsonView data(final Object data) {
        this.data = data;
        return this;
    }

    @JsonProperty("Message")
    public String getMessage() {
        return this.message;
    }

    public CommonJsonView message(final String message) {
        this.message = message;
        return this;
    }

    static CommonJsonView newView(final Status status) {
        final CommonJsonView view = new CommonJsonView();
        final DateTime time = DateTime.now(DateTimeZone.UTC);
        return view.status(status).addMetaData("Date", time.toString(ISODateTimeFormat.dateTime()));
    }
}

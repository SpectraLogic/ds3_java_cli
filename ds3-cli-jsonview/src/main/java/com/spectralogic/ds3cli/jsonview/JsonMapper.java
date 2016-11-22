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

package com.spectralogic.ds3cli.jsonview;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.spectralogic.ds3cli.utils.SterilizeString;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public final class JsonMapper {
    private final static ObjectWriter writer;
    private JsonMapper() {
    }

    static {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(DATE_FORMAT);
        mapper.registerModule(new GuavaModule());
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    public static String toJson(final Object obj) throws JsonProcessingException {
        return SterilizeString.toUnix(writer.writeValueAsString(obj));
    }
}

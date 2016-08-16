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

package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3client.networking.FailedRequestException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;
import java.util.Map;

public class CommandExceptionJsonView implements View<CommandException> {
    @Override
    public String render(final CommandException obj) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.ERROR);
        final Map<String, String> jsonBackingMap = new TreeMap<>();

        view.message(obj.getMessage());

        try {
            final ObjectMapper mapper = new ObjectMapper();
            if (obj.getCause() != null) {
                if (obj.getCause() instanceof FailedRequestException) {
                    final FailedRequestException ce = (FailedRequestException) obj.getCause();
                    jsonBackingMap.put("StatusCode", Integer.toString(ce.getStatusCode()));
                    jsonBackingMap.put("ApiErrorMessage", ce.getResponseString());
                } else {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final PrintWriter pOut = new PrintWriter(out);
                    obj.printStackTrace(pOut);
                    try {
                        jsonBackingMap.put("StackTrace", out.toString("utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(view.data(jsonBackingMap));
        }
        catch(final Exception e) {
            return "Failed to render error: " + e.getMessage();
        }
    }
}

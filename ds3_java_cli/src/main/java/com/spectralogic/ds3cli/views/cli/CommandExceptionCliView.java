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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.exceptions.CommandException;
import com.spectralogic.ds3cli.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CommandExceptionCliView implements View<CommandException> {

    private final static Logger LOG = LoggerFactory.getLogger(CommandExceptionCliView.class);

    public String render(final CommandException obj) throws JsonProcessingException {
        final StringBuilder builder = new StringBuilder();
        builder.append(obj.getMessage());

        try {
            if (LOG.isInfoEnabled()) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final PrintWriter pOut = new PrintWriter(out);
                obj.printStackTrace(pOut);
                builder.append('\n');
                builder.append(out.toString("utf-8"));
            }
        } catch (final UnsupportedEncodingException e) {
            System.out.println("ERROR: Failed to parse error stack TRACE as message, defaulting to standard error printing");
            obj.getCause().printStackTrace();
        }

        return builder.toString();
    }
}

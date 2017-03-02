/*
 * *****************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.util;

import java.io.Closeable;
import java.io.IOException;

public class CommandListenerImpl implements CommandListener {

    private final StringBuilder builder;

    public CommandListenerImpl() {
        builder = new StringBuilder("");
    }

    public void append(final String message) {
        builder.append(message);
    }

    public String toString() {
        return builder.toString();
    }

    @Override
    public void close() throws IOException {
        // nothing to do here
    }
}

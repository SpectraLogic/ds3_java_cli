/*
 * ******************************************************************************
 * Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file.
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ******************************************************************************
 */
package com.spectralogic.ds3cli.logging;

class StdoutLoggerImpl implements Logging.Logger {
    private boolean verbose = false;

    @Override
    public void setVerbose(boolean value) {
        this.verbose = value;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void log(final String message) {
        if (verbose) System.out.println(message);
    }

    @Override
    public void logf(final String message, final Object... args) {
        if (verbose) System.out.printf(message + "\n", args);
    }
}

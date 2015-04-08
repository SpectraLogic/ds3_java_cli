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

package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.Result;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

import java.util.concurrent.Callable;

public abstract class CliCommand<T extends Result> implements Callable<T> {

    private final Ds3Provider ds3Provider;

    public CliCommand(final Ds3Provider ds3Provider) {
        this.ds3Provider = ds3Provider;
    }

    protected Ds3Client getClient() {
        return this.ds3Provider.getClient();
    }

    protected Ds3ClientHelpers getClientHelpers() {
        return this.ds3Provider.getClientHelpers();
    }

    public abstract CliCommand init(final Arguments args) throws Exception;
}

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

package com.spectralogic.ds3cli;

import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

public class Ds3ProviderImpl implements Ds3Provider {

    private final Ds3Client client;
    private final Ds3ClientHelpers helpers;

    public Ds3ProviderImpl(final Ds3Client client, final Ds3ClientHelpers helpers) {
        this.client = client;
        this.helpers = helpers;
    }

    @Override
    public Ds3Client getClient() {
        return this.client;
    }

    @Override
    public Ds3ClientHelpers getClientHelpers() {
        return this.helpers;
    }
}

/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.DetailedS3Object;

public class GetDetailedObjectsResult implements Result<Iterable<DetailedS3Object>> {
    final private Iterable<DetailedS3Object> objects;

    public GetDetailedObjectsResult(final Iterable<DetailedS3Object> objects) {
        this.objects = objects;
    }

    @Override
    public Iterable<DetailedS3Object> getResult() {
        return this.objects;
    }
}

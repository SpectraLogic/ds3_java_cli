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

package com.spectralogic.ds3cli.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.command.PutBulk;

public class PutBulkResult implements Result {
    @JsonProperty("status_message")
    private final String result;

    @JsonProperty("ignored_files")
    @JacksonXmlElementWrapper(
            useWrapping = true
    )
    private final ImmutableList<PutBulk.IgnoreFile> ignoredFiles;

    public PutBulkResult(final String result){
        this.result = result;
        this.ignoredFiles = null;
    }

    public PutBulkResult(final String result, final ImmutableList<PutBulk.IgnoreFile> ignoredFiles) {
        this.result = result;
        this.ignoredFiles = ignoredFiles;
    }

    final public String getResult(){
        return this.result;
    }

    public ImmutableList<PutBulk.IgnoreFile> getIgnoredFiles() {
        return this.ignoredFiles;
    }
}

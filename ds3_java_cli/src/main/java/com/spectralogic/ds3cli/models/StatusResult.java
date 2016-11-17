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

public class StatusResult implements Result<String> {
    @JsonProperty("status_message")
    final private String    result;
    @JsonProperty("status_code")
    final private int       status;

    @Override
    final public String getResult(){
        return this.result;
    }

    final public int getStatus(){
        return this.status;
    }

    public StatusResult(final String result){
        this.result = result;
        this.status = 0;
    }

    public StatusResult(final String result, final int status){
        this.result = result;
        this.status = status;
    }
}

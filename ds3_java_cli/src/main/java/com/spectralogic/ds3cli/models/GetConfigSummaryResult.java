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

import java.util.Map;

public class GetConfigSummaryResult implements Result<Map<String, Result>> {

    // Map for JSON output
    private final Map<String, Result> result;

    // String for CLI output
    private final String summary;

    public GetConfigSummaryResult(final Map<String, Result> result, final String summary) {
        this.summary = summary;
        this.result = result;
    }

    public Map<String, Result> getResult() {
        return this.result;
    }
    public String getSummary() { return this.summary; }

}

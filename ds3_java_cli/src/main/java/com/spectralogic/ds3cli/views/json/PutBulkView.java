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
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.jsonview.CommonJsonView;
import com.spectralogic.ds3cli.jsonview.JsonMapper;
import com.spectralogic.ds3cli.models.PutBulkResult;
import com.spectralogic.ds3client.utils.Guard;

public class PutBulkView implements View<PutBulkResult> {
    @Override
    public String render(final PutBulkResult result) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);

        if (Guard.isNullOrEmpty(result.getIgnoredFiles())) {
            view.message(result.getResult());
        }
        else {
            view.data(result);
        }

        return JsonMapper.toJson(view);
    }
}

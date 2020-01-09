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

package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetServiceResult;
import com.spectralogic.ds3cli.util.JsonMapper;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

public class GetServiceView extends View<GetServiceResult> {

    @Override
    public String render(final GetServiceResult obj) throws JsonProcessingException {
        final ListAllMyBucketsResult result = obj.getResult();
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);

        if (result == null || null == result.getBuckets()) {
            view.message("You do not have any buckets");
            return JsonMapper.toJson(view);
        }

        return JsonMapper.toJson(view.data(result));
    }

}

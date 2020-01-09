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
import com.spectralogic.ds3cli.models.GetCacheStateResult;
import com.spectralogic.ds3cli.util.JsonMapper;
import com.spectralogic.ds3client.models.CacheFilesystemInformation;
import com.spectralogic.ds3client.utils.Guard;

import java.util.List;

public class GetCacheStateView extends View<GetCacheStateResult> {

    @Override
    public String render(final GetCacheStateResult obj) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);

        final List<CacheFilesystemInformation> cacheFilesystemInfo = obj.getResult();
        if(Guard.isNullOrEmpty(cacheFilesystemInfo)){
            return "No valid Cache Filesystems on remote appliance";
        }

        return JsonMapper.toJson(view.data(cacheFilesystemInfo));
    }
}

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMultimap;
import com.spectralogic.ds3cli.api.View;
import com.spectralogic.ds3cli.jsonview.CommonJsonView;
import com.spectralogic.ds3cli.jsonview.JsonMapper;
import com.spectralogic.ds3cli.models.HeadObjectResult;
import com.spectralogic.ds3client.commands.HeadObjectResponse;
import com.spectralogic.ds3client.networking.Metadata;

import java.util.List;

public class HeadObjectView implements View<HeadObjectResult> {
    @Override
    public String render(final HeadObjectResult obj) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);

        final HeaderModel model = new HeaderModel(obj.getResult(), obj.getMetadata());
        return JsonMapper.toJson(view.data(model));
    }

    private static class HeaderModel {

        @JsonProperty("Metadata")
        private final ImmutableMultimap<String, String> metadata;

        @JsonProperty("Status")
        private final HeadObjectResponse.Status status;

        public HeaderModel(final HeadObjectResponse.Status status, final Metadata metadata) {
            this.status = status;
            this.metadata = toMap(metadata);
        }

        private static ImmutableMultimap<String, String> toMap(final Metadata metadata) {
            final ImmutableMultimap.Builder<String, String> mapBuilder = ImmutableMultimap.builder();

            for (final String entry : metadata.keys()) {
                final List<String> values = metadata.get(entry);
                mapBuilder.putAll(entry, values);
            }

            return mapBuilder.build();
        }

        public ImmutableMultimap<String, String> getMetadata() {
            return metadata;
        }

        public HeadObjectResponse.Status getStatus() {
            return status;
        }
    }
}

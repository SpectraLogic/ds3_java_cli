package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMultimap;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.HeadObjectResult;
import com.spectralogic.ds3cli.util.JsonMapper;
import com.spectralogic.ds3client.commands.HeadObjectResponse;
import com.spectralogic.ds3client.networking.Metadata;

import java.util.List;

public class HeadObjectView implements View<HeadObjectResult> {
    @Override
    public String render(final HeadObjectResult obj) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);

        final HeaderModel model = new HeaderModel(obj.getStatus(), obj.getMetadata());
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

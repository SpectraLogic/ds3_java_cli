package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GetServiceView implements View<ListAllMyBucketsResult> {

    @Override
    public String render(final ListAllMyBucketsResult obj) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        if( (obj == null) || (null == obj.getBuckets()) ){
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString("You do not have any buckets");
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

}

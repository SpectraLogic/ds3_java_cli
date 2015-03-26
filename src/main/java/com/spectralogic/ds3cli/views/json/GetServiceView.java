package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetServiceResult;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class GetServiceView implements View<GetServiceResult> {

    @Override
    public String render(final GetServiceResult obj) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final ListAllMyBucketsResult result = obj.getResult();


        if( (result == null) || (null == result.getBuckets()) ){
            final Map<String, String> jsonBackingMap = new HashMap<>();
            jsonBackingMap.put("message", "You do not have any buckets");
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonBackingMap);
        }
        else {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        }
    }

}

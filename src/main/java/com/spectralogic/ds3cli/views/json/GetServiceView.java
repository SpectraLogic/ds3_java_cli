package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetServiceResult;
import com.spectralogic.ds3cli.util.JsonMapper;
import com.spectralogic.ds3client.models.ListAllMyBucketsResult;

public class GetServiceView implements View<GetServiceResult> {

    @Override
    public String render(final GetServiceResult obj) throws JsonProcessingException {
        final ListAllMyBucketsResult result = obj.getResult();
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);

        if( (result == null) || (null == result.getBuckets()) ){
            view.message("You do not have any buckets");
            return JsonMapper.toJson(view);
        }

        return JsonMapper.toJson(view.data(result));
    }

}

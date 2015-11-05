package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetJobResult;
import com.spectralogic.ds3cli.util.JsonMapper;

public class GetJobView implements View<GetJobResult> {
    @Override
    public String render(final GetJobResult obj) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);
        return JsonMapper.toJson(view.data(obj));
    }
}

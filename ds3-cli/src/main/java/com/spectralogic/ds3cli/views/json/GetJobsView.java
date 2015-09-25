package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetJobsResult;
import com.spectralogic.ds3cli.util.JsonMapper;

public class GetJobsView implements View<GetJobsResult> {
    @Override
    public String render(final GetJobsResult obj) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);
        view.data(obj.getJobs());
        return JsonMapper.toJson(view);
    }
}

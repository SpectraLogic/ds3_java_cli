package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetPhysicalPlacementWithFullDetailsResult;
import com.spectralogic.ds3cli.util.JsonMapper;

public class GetPhysicalPlacementWithFullDetailsView  implements View<GetPhysicalPlacementWithFullDetailsResult> {
    @Override
    public String render(final GetPhysicalPlacementWithFullDetailsResult result) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);
        return JsonMapper.toJson(view.data(result));
    }
}

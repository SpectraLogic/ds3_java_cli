package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetTapesResult;
import com.spectralogic.ds3cli.util.JsonMapper;
import com.spectralogic.ds3client.models.tape.Tapes;

public class GetTapesView implements View<GetTapesResult> {
    @Override
    public String render(final GetTapesResult obj) throws JsonProcessingException {
       final Tapes result = obj.getTapes();
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.OK);

        if( (result == null) || (null == result.getTapes()) ){
            view.message("You do not have any buckets");
            return JsonMapper.toJson(view);
        }

        return JsonMapper.toJson(view.data(result));
    }
}

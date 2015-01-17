package com.spectralogic.ds3cli.views.cli;

import com.spectralogic.ds3cli.models.GetObjectResult;
import com.spectralogic.ds3cli.View;

public class GetObjectView implements View<GetObjectResult> {
    @Override
    public String render(final GetObjectResult result) {
        return result.getResult();
    }
}

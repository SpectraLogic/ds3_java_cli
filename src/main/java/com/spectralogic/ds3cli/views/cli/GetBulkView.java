package com.spectralogic.ds3cli.views.cli;

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.GetBulkResult;

public class GetBulkView implements View<GetBulkResult> {
    @Override
    public String render(final GetBulkResult result) {
        return result.getResult();
    }
}

package com.spectralogic.ds3cli.views.cli;

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.PutBulkResult;

public class PutBulkView implements View<PutBulkResult> {
    @Override
    public String render(final PutBulkResult result) {
        return result.getResult();
    }
}

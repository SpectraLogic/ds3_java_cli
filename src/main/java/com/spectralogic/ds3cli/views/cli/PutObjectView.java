package com.spectralogic.ds3cli.views.cli;

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.PutObjectResult;

public class PutObjectView implements View<PutObjectResult> {
    @Override
    public String render(final PutObjectResult result) {
        return result.getResult();
    }
}

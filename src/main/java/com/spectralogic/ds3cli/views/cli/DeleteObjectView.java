package com.spectralogic.ds3cli.views.cli;

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.DeleteObjectResult;

public class DeleteObjectView implements View<DeleteObjectResult> {
    @Override
    public String render(final DeleteObjectResult result) {
        return result.getResult();
    }
}

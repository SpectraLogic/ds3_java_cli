package com.spectralogic.ds3cli.views.cli;

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.DeleteBucketResult;

public class DeleteBucketView implements View<DeleteBucketResult> {
    @Override
    public String render(final DeleteBucketResult result) {
        return result.getResult();
    }
}

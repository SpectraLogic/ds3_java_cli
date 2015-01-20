package com.spectralogic.ds3cli.views.cli;

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.PutBucketResult;

public class PutBucketView implements View<PutBucketResult> {
    @Override
    public String render(final PutBucketResult result) {
        return result.getResult();
    }
}

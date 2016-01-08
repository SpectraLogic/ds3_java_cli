package com.spectralogic.ds3cli.views.json;

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.models.PerformanceResult;

public class PerformanceView implements View<PerformanceResult> {

    @Override
    public String render(final PerformanceResult result) {
        return "Json output is not supported with the performance command";
    }
}

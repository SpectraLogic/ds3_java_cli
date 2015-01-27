package com.spectralogic.ds3cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.ArrayList;

enum ViewType {
    CLI, JSON;

    public static String valuesString() {
        final ArrayList<ViewType> list = Lists.newArrayList(ViewType.values());
        return Joiner.on(", ").join(Lists.transform(list, new Function<ViewType, String>() {
            @Override
            public String apply(final ViewType input) {
                return input.toString().toLowerCase();
            }
        }));
    }
}
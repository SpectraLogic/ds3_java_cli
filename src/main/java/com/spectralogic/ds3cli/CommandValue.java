package com.spectralogic.ds3cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.ArrayList;

public enum CommandValue {
GET_SERVICE, GET_BUCKET, GET_OBJECT, PUT_BUCKET, PUT_OBJECT, DELETE_BUCKET, DELETE_OBJECT, GET_BULK, PUT_BULK;

    public static String valuesString() {
        final ArrayList<CommandValue> list = Lists.newArrayList(CommandValue.values());
        return Joiner.on(", ").join(Lists.transform(list, new Function<CommandValue, String>() {
            @Override
            public String apply(final CommandValue input) {
                return input.toString().toLowerCase();
            }
        }));
    }
}

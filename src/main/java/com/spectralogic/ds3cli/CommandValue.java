/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.ArrayList;

public enum CommandValue {
GET_SERVICE, GET_BUCKET, GET_OBJECT, PUT_BUCKET, PUT_OBJECT, DELETE_BUCKET, DELETE_OBJECT, GET_BULK, PUT_BULK,
    DELETE_TAPE_DRIVE, DELETE_TAPE_PARTITION, GET_JOBS, GET_JOB, DELETE_JOB, SYSTEM_INFORMATION, HEAD_OBJECT, DELETE_FOLDER;

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

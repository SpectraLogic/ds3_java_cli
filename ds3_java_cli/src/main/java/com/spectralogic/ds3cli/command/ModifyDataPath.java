/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.command;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.ArgumentFactory;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3client.commands.spectrads3.ModifyDataPathBackendSpectraS3Request;
import org.apache.commons.cli.Option;

public class ModifyDataPath extends CliCommand<DefaultResult> {

    private final static ImmutableList<Option> requiredArgs = ImmutableList.of(ArgumentFactory.VERIFY_PERCENT);

    private int verifyLastPercent;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        addRequiredArguments(requiredArgs, args);
        args.parseCommandLine();
        this.verifyLastPercent = args.getVerifyLastPercent();
        if (this.verifyLastPercent > 100 || this.verifyLastPercent < 0) {
            throw new IllegalArgumentException("'verifyLastPercent' must be between 0 and 100");
        }
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {

        if (!Utils.isVersionSupported(getClient(), "3.2.3")) {
            throw new RuntimeException("'modify_data_path' is supported with BP version 3.2.3 or later");
        }

        getClient().modifyDataPathBackendSpectraS3(new ModifyDataPathBackendSpectraS3Request().withPartiallyVerifyLastPercentOfTapes(verifyLastPercent));

        return new DefaultResult("Updated data path backend");
    }
}

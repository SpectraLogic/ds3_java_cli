/*
 * ******************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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
package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.models.RecoveryJob;

interface RecoverableCommand {
    /**
     * Load args from JSON
     * must override this method to implement
     * @param job RecoveryJob object
     * @returns this
     * @throws Exception parsing and argument exceptions
     */
    public CliCommand init(final RecoveryJob job) throws Exception;

}

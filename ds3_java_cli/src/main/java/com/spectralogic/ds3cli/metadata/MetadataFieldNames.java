/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.metadata;

public final class MetadataFieldNames {
    public static final String LAST_MODIFIED_TIME = "ds3-last-modified-time";
    public static final String LAST_ACCESSED_TIME = "ds3-last-access-time";
    public static final String CREATION_TIME = "ds3-create-time";
    public static final String CHANGED_TIME = "ds3-ctime";
    public static final String OWNER = "ds3-owner";
    public static final String GROUP = "ds3-group";
    public static final String MODE = "ds3-mode";

    private MetadataFieldNames() {
        // Intentionally not implemented
    }
}

/*
 * ***************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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

import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.GetDetailedObjectsResult;
import com.spectralogic.ds3cli.views.json.DataView;

/**
 * Use the DetailedObjectsPhysicalView to format output
 * by physical placement: one line for each instance on tape
 */
public class GetDetailedObjectsPhysical extends GetDetailedObjects {

    @Override
    public View<GetDetailedObjectsResult> getView() {
        if (viewType == ViewType.JSON) {
            // sorting performed in view -- json would be no different
            return new DataView<>();
        } else if (viewType == ViewType.CSV) {
            return new com.spectralogic.ds3cli.views.csv.DetailedObjectsPhysicalView();
        } else {
            return new com.spectralogic.ds3cli.views.cli.DetailedObjectsPhysicalView();
        }
    }

}

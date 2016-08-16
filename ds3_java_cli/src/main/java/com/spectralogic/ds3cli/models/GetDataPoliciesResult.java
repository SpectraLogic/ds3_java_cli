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

package com.spectralogic.ds3cli.models;

import com.spectralogic.ds3client.models.DataPolicy;
import com.spectralogic.ds3client.models.DataPolicyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GetDataPoliciesResult implements Result {

    private final DataPolicyList dataPolicyListResult;

    public GetDataPoliciesResult(final DataPolicyList policies) {
        this.dataPolicyListResult = policies;
    }

    // build from DataPolicy to leverage views
    public GetDataPoliciesResult(final DataPolicy policy) {

        // put it in a List of one to use data policies view
        this.dataPolicyListResult = new DataPolicyList();
        List<DataPolicy> listOfOne = new ArrayList<DataPolicy>();
        listOfOne.add(policy);
        this.dataPolicyListResult.setDataPolicies(listOfOne);
    }

    public DataPolicyList getPolicyList() {
        return dataPolicyListResult;
    }

    public Iterator<DataPolicy> getObjIterator() {
        DataPolicyList policylist = getPolicyList();
        if (policylist != null) {
            List<DataPolicy> policies = policylist.getDataPolicies();
            if (policies != null) {
                return policies.iterator();
            }
        }
        return Collections.emptyListIterator();
    }
}



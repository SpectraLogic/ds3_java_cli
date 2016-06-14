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

package com.spectralogic.ds3cli.models;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3client.models.SpectraUser;
import com.spectralogic.ds3client.models.SpectraUserList;
import com.spectralogic.ds3client.utils.Guard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GetUsersResult implements Result {

    private SpectraUserList spectraUserListResult;

    public GetUsersResult(final SpectraUserList users) {
        this.spectraUserListResult = users;
    }

    // build from SpectraUser to leverage views
    public GetUsersResult(final SpectraUser user) {

        // put it in a List of one to use data users view
        this.spectraUserListResult = new SpectraUserList();
        final ImmutableList<SpectraUser> listOfOne = ImmutableList.of(user);
        this.spectraUserListResult.setSpectraUsers(listOfOne);
    }

    public SpectraUserList getuserList() {
        return spectraUserListResult;
    }

    public Iterator<SpectraUser> getObjIterator() {
        final SpectraUserList userlist = getuserList();
        if (userlist != null) {
            final List<SpectraUser> users = userlist.getSpectraUsers();
            if (Guard.isNotNullAndNotEmpty(users)) {
                return users.iterator();
            }
        }
        return Collections.emptyListIterator();
    }
}



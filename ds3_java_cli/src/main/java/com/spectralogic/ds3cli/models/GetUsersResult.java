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

import com.spectralogic.ds3client.models.SpectraUser;
import com.spectralogic.ds3client.models.SpectraUserList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GetUsersResult implements Result {

    private SpectraUserList SpectraUserListResult;

    public GetUsersResult(final SpectraUserList users) {
        this.SpectraUserListResult = users;
    }

    // build from SpectraUser to leverage views
    public GetUsersResult(final SpectraUser user) {

        // put it in a List of one to use data users view
        this.SpectraUserListResult = new SpectraUserList();
        final List<SpectraUser> listOfOne = new ArrayList<SpectraUser>();
        listOfOne.add(user);
        this.SpectraUserListResult.setSpectraUsers(listOfOne);
    }

    public SpectraUserList getuserList() {
        return SpectraUserListResult;
    }

    public Iterator<SpectraUser> getObjIterator() {
        final SpectraUserList userlist = getuserList();
        if (userlist != null) {
            final List<SpectraUser> users = userlist.getSpectraUsers();
            if (users != null) {
                return users.iterator();
            }
        }
        return Collections.emptyListIterator();
    }
}



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

package com.spectralogic.ds3cli.integration;

import com.spectralogic.ds3cli.Main;
import com.spectralogic.ds3cli.integration.Util;
import org.junit.Test;

public class FeatureIntegration_Test {



    @Test
    public void getJobs() {
        final String bucket = "get_jobs_test";
        //Main.main(new String [] {"c", "put_bucket", "b", bucket});
        try {
            //Main.main(new String[] {"http", "e", "192.168.56.101:8080", "k", "8qHN6TUE -a c3BlY3RyYQ==", "h"});
            Main.main(new String[] {"-h"});
        } catch (final Exception e) {
            System.out.println("TEST: catch");
        } finally {
            System.out.println("TEST: final");
        }
        //Util.loadBookTestData(bucket);

        //Main.main(new String[] {"c", "get_jobs", "options"});
    }

}

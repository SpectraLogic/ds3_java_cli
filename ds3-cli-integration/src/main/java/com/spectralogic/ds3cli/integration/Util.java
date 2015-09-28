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

public class Util {
    public static final String RESOURCE_BASE_NAME = "books/";

    private static final String[] BOOKS = {"beowulf.txt", "sherlock_holmes.txt", "tale_of_two_cities.txt", "ulysses.txt"};

    private Util() {}

    private static void loadTestFile(final String bucketName, final String fileName) {
        Main.main(new String[] {"-c", "put_object", "-b", bucketName, "-o", fileName});
    }

    public static void loadBookTestData(final String bucketName) {
        for(String book : BOOKS) {
            loadTestFile(bucketName, book);
        }
    }

}

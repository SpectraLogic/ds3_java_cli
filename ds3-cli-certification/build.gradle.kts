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

plugins {
    id("base-compile-conventions")
    `java-test-fixtures`
}

dependencies {
    implementation(platform(libs.ds3Bom))
    implementation(platform(libs.jacksonBom))

    implementation(project(":ds3_java_cli"))
    implementation(project(":ds3-cli-helpers"))

    implementation(libs.ds3Sdk)
    implementation(libs.findbugs)
    implementation(libs.slf4jApi)

    testImplementation(testFixtures(project(":ds3-cli-helpers")))

    testImplementation(libs.commonsIo)
    testImplementation(libs.junit)
    testImplementation(libs.hamcrestLib)
}

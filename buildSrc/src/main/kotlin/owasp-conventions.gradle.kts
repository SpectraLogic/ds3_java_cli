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
    id("org.owasp.dependencycheck")
}

dependencyCheck {
    //skipConfigurations = listOf("ktlint", "ktlintBaselineReporter", "detekt")
    suppressionFile = "project_files/owasp/dependency-check-suppression.xml"
    // fail the build if any vulnerable dependencies are identified (CVSS score > 0)
    failBuildOnCVSS = 0F
    //suppressionFile = "project_files/owasp/dependency-check-suppression.xml"
    // node audit for yarn was recently enabled and it's not quite working yet.
    // try again when updating the dependency check plugin.
    // last tried: v8.1.2
    analyzers.nodeAudit.yarnEnabled = false
}

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
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.5.0")
}

rootProject.name = "ds3_java_cli"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // If you really need to resolve a dependency from your local maven cache, uncomment the line below.
        // mavenLocal()
    }
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include(
    "ds3-cli-helpers",
    "ds3-cli-integration",
    "ds3-cli-certification",
    "ds3_java_cli"
)

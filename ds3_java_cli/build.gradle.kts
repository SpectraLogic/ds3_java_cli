import java.util.Date

/*
 * ***************************************************************************
 *   Copyright 2014-2023 Spectra Logic Corporation. All Rights Reserved.
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
    application
}

dependencies {
    implementation(platform(libs.ds3Bom))
    implementation(platform(libs.jacksonBom))

    api(libs.jacksonDatatypeGuava)
    api(libs.commonsCli)
    api(libs.jodaTime)

    implementation(libs.commonsCsv)
    implementation(libs.ds3Sdk)
    implementation(libs.guava)
    implementation(libs.guiceCore)
    implementation(libs.jacksonAnnotations)
    implementation(libs.jacksonCore)
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonDataformatXml)
    implementation(libs.kotlinCoroutinesCore)
    implementation(libs.logbackClassic)

    implementation(fileTree("lib") {
        include("*.jar")
    })

    testImplementation(libs.commonsIo)
    testImplementation(libs.junit)
    testImplementation(libs.hamcrestLib)
    testImplementation(libs.powermockMockito)
    testImplementation(libs.powermockJunit4)
}

application {
    mainClass.set("com.spectralogic.ds3cli.Main")
}

val genConfigProperties = tasks.register("genConfigProperties") {
    val configFile = sourceSets.main.get().output.resourcesDir!!.resolve("ds3_cli.properties")
    outputs.file(configFile)
    doLast {
        configFile.writeText("""
            version=${version}
            build.date=${Date().toString()}
            """.trimIndent())
    }
}

tasks.compileTestKotlin {
    dependsOn(genConfigProperties)
}

tasks.jar {
    dependsOn(genConfigProperties)
}

// convenience task name for github tests
tasks.register<Test>("test8") {
    dependsOn(tasks.test)
}

tasks.register<Test>("test11") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.ADOPTIUM)
        // powermock warnings for Java >= 11
        jvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.util.regex=ALL-UNNAMED",
            "--add-opens", "java.base/java.text=ALL-UNNAMED",
            "--add-opens", "java.base/java.io=ALL-UNNAMED",
            "--add-opens", "java.base/sun.nio.fs=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
        )
    })
}

tasks.register<Test>("test17") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
        // powermock warnings for Java >= 11
        jvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.util.regex=ALL-UNNAMED",
            "--add-opens", "java.base/java.text=ALL-UNNAMED",
            "--add-opens", "java.base/java.io=ALL-UNNAMED",
            "--add-opens", "java.base/sun.nio.fs=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            )
    })
}

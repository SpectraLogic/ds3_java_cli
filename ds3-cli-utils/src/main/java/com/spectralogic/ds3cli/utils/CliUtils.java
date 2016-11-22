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

package com.spectralogic.ds3cli.utils;

import com.google.common.base.Joiner;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public final class CliUtils {

    private final static String QUOTE = Pattern.quote(".");

    public final static String MINIMUM_VERSION_SUPPORTED = "1.2";

    public static boolean isVersionSupported(final Ds3Client client) throws IOException {
        return isVersionSupported(client, MINIMUM_VERSION_SUPPORTED);
    }

    public static boolean isVersionSupported(final Ds3Client client, final String minVersion) throws IOException {
        final String buildInfo = client.getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request()).getSystemInformationResult().getBuildInformation().getVersion();
        final String[] buildInfoArr = buildInfo.split(QUOTE);
        final String[] versionInfo = minVersion.split(QUOTE);

        if (versionInfo.length > 3) {
            throw new IllegalArgumentException("The version string can have 3 numbers");
        }

        for (int i = 0; i < versionInfo.length && i < buildInfoArr.length; i++) {
            final int i1 = Integer.parseInt(buildInfoArr[i]);
            final int i2 = Integer.parseInt(versionInfo[i]);
            if (i1 > i2) return true;
            if (i1 < i2) return false;
        }
        return true;
    }

    public static boolean isPipe() throws IOException {
        return System.in.available() > 0;
    }

    /**
     *  Takes an enum.values() Object  array  and returns all possible values
     *  in a comma-delimited string
     */
    public static String printEnumOptions(final Object[] optionEnum) {
        final Joiner joiner = Joiner.on(", ");
        return joiner.join(optionEnum);
    }

    public static Properties readProperties(final String propertyFile) throws IOException {
        final Properties props = new Properties();
        final InputStream input = CliUtils.class.getClassLoader().getResourceAsStream(propertyFile);
        if (input != null) {
            props.load(input);
            return props;
        }
        props.put("version", "N/a");
        props.put("build.date", "N/a");
        return props;
    }

    public static String getVersion(final Properties properties) {
        return properties.get("version").toString();
    }

    public static String getBuildDate(final Properties properties) {
        return properties.get("build.date").toString();
    }

    public static String getPlatformInformation() {
        return String.format("Java Version: {%s}\n", System.getProperty("java.version"))
            + String.format("Java Vendor: {%s}\n", System.getProperty("java.vendor"))
            + String.format("JVM Version: {%s}\n", System.getProperty("java.vm.version"))
            + String.format("JVM Name: {%s}\n", System.getProperty("java.vm.name"))
            + String.format("OS: {%s}\n", System.getProperty("os.name"))
            + String.format("OS Arch: {%s}\n", System.getProperty("os.arch"))
            + String.format("OS Version: {%s}\n", System.getProperty("os.version"));
    }

    /**
     * Lookup help for '--help' COMMAND from resource file
     * (Override or add help text to resources/com/spectralogic/dscli/help.properties
     * @param command
     * @return
     */
    public static String getVerboseHelp(final String command) {
        return CommandHelpText.getHelpText(command.toUpperCase());
    }
}

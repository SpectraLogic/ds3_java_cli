/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3cli.command;

import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.models.Result;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.views.cli.DefaultView;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

import java.util.concurrent.Callable;

import com.spectralogic.ds3cli.util.CommandHelpText;

public abstract class CliCommand<T extends Result> implements Callable<T> {

    private Ds3Provider ds3Provider;
    private FileUtils fileUtils;

    // for service provider instantiation
    public CliCommand() {
        this.ds3Provider = null;
        this.fileUtils = null;
    }

    public CliCommand withProvider(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        this.ds3Provider = ds3Provider;
        this.fileUtils = fileUtils;
        return this;
    }

    public CliCommand(final Ds3Provider ds3Provider, final FileUtils fileUtils) {
        this.ds3Provider = ds3Provider;
        this.fileUtils = fileUtils;
    }

    protected Ds3Client getClient() {
        return this.ds3Provider.getClient();
    }

    protected Ds3ClientHelpers getClientHelpers() {
        return this.ds3Provider.getClientHelpers();
    }

    protected Ds3Provider getProvider() {
        return this.ds3Provider;
    }

    protected FileUtils getFileUtils() {
        return this.fileUtils;
    }

    public abstract CliCommand init(final Arguments args) throws Exception;


    // help for '--help' command
    public String getLongHelp(final String command) {
        return CommandHelpText.getHelpText(command) ;
    }

    public View<T> getView(final ViewType viewType) {
        if (viewType == ViewType.JSON) {
            return (View<T>) new com.spectralogic.ds3cli.views.json.DefaultView();
        }
        return (View<T>) new DefaultView();
    }

    public static String getPlatformInformation() {
        StringBuilder ret = new StringBuilder();
        ret.append(String.format("Java Version: {%s}\n", System.getProperty("java.version")));
        ret.append(String.format("Java Vendor: {%s}\n", System.getProperty("java.vendor")));
        ret.append(String.format("JVM Version: {%s}\n", System.getProperty("java.vm.version")));
        ret.append(String.format("JVM Name: {%s}\n", System.getProperty("java.vm.name")));
        ret.append(String.format("OS: {%s}\n", System.getProperty("os.name")));
        ret.append(String.format("OS Arch: {%s}\n", System.getProperty("os.arch")));
        ret.append(String.format("OS Version: {%s}\n", System.getProperty("os.version")));
        return ret.toString();
    }

}

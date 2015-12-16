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

import com.google.common.collect.Lists;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.models.GetObjectResult;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3cli.util.SyncUtils;
import com.spectralogic.ds3cli.util.Utils;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.FileObjectGetter;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import org.apache.commons.cli.MissingOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.util.List;

public class GetObject extends CliCommand<GetObjectResult> {

    private final static Logger LOG = LoggerFactory.getLogger(GetObject.class);

    private String bucketName;
    private String objectName;
    private String prefix;
    private boolean sync;

    public GetObject(final Ds3Provider provider, final FileUtils fileUtils) {
        super(provider, fileUtils);
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        bucketName = args.getBucket();
        if (bucketName == null) {
            throw new MissingOptionException("The get object command requires '-b' to be set.");
        }

        objectName = args.getObjectName();
        if (objectName == null) {
            throw new MissingOptionException("The get object command requires '-o' to be set.");
        }

        prefix = args.getDirectory();
        if (prefix == null) {
            prefix = ".";
        }

        if (args.isSync()) {
            LOG.info("Using sync command");
            this.sync = true;
        }

        return this;
    }

    @Override
    public GetObjectResult call() throws Exception {
        try {
            final Ds3ClientHelpers helpers = getClientHelpers();
            final Path filePath = Paths.get(prefix, objectName);
            LOG.info("Output path: " + filePath.toString());

            final Ds3Object ds3Obj = new Ds3Object(objectName.replace("\\", "/"));
            if (sync && Utils.fileExists(filePath)) {
                if (SyncUtils.needToSync(helpers, bucketName, filePath, ds3Obj.getName(), false)) {
                    Transfer(helpers, ds3Obj);
                    return new GetObjectResult("SUCCESS: Finished syncing object.");
                }
                else {
                    return new GetObjectResult("SUCCESS: No need to sync " + objectName);
                }
            }

            Transfer(helpers, ds3Obj);
            return new GetObjectResult("SUCCESS: Finished downloading object.  The object was written to: " + filePath);
        }
        catch(final FailedRequestException e) {
            if(e.getStatusCode() == 500) {
                LOG.info(e.getMessage());
                if (LOG.isInfoEnabled()) {
                    e.printStackTrace();
                }
                throw new CommandException( "Error: Cannot communicate with the remote DS3 appliance.", e);
            }
            else if(e.getStatusCode() == 404) {
                if (LOG.isInfoEnabled()) {
                    e.printStackTrace();
                }
                throw new CommandException( "Error: " + e.getMessage(), e);
            }
            else {
                if (LOG.isInfoEnabled()) {
                    e.printStackTrace();
                }
                throw new CommandException( "Error: Encountered an unknown error of ("+ e.getStatusCode() +") while accessing the remote DS3 appliance.", e);
            }
        }
    }

    private void Transfer(final Ds3ClientHelpers helpers, final Ds3Object ds3Obj) throws IOException, SignatureException, XmlProcessingException {
        final List<Ds3Object> ds3ObjectList = Lists.newArrayList(ds3Obj);
        final Ds3ClientHelpers.Job job = helpers.startReadJob(bucketName, ds3ObjectList);
        job.transfer(new FileObjectGetter(Paths.get(prefix)));
    }
}

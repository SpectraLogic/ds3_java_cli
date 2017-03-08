/*
 * ******************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.spectralogic.ds3cli.util.Constants;

import java.lang.String;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JacksonXmlRootElement(namespace = "Data")
public class RecoveryJob {

    public RecoveryJob() {
    }

    public RecoveryJob(final BulkJobType type) {
        setJobType(type);
        setRecoveryCommand("recover_" + type.name().toLowerCase());
        setCreationDate(new Date());
    }

    @JsonProperty("CreationDate")
    private Date creationDate;

    @JsonProperty("RecoveryCommand")
    private String recoveryCommand;

    @JsonProperty("JobType")
    private BulkJobType jobType;

    @JsonProperty("Prefixes")
    private List<String> prefixes;

    @JsonProperty("BucketName")
    private String bucketName;

    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("Directory")
    private String directory;

    @JsonProperty("NumberOfThreads")
    private int numberOfThreads;


    public Date getCreationDate() {
        return this.creationDate;
    }
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public UUID getId() {
        return this.id;
    }
    public void setId(final UUID id) {
        this.id = id;
    }

    public String getBucketName() {
        return this.bucketName;
    }
    public void setBucketName(final String name) {
        this.bucketName = name;
    }

    public List<String> getPrefixes() {
        return this.prefixes;
    }
    public void setPrefix(final List<String> prefixes) {
        this.prefixes = prefixes;
    }

    public BulkJobType getJobType() { return this.jobType; }
    public void setJobType(final BulkJobType type) { this.jobType = type; }

    public String getRecoveryCommand() { return this.recoveryCommand; }
    public void setRecoveryCommand(final String command) { this.recoveryCommand = command; }

    public String getDirectory() { return this.directory; }
    public void setDirectory(final String dir) {this.directory = dir; }
    public void setDirectory(final Path path) {
        if (path != null) {
            this.directory = path.toString();
        }
    }

    public int getNumberOfThreads() { return this.numberOfThreads; }
    public void setNumberOfThreads(final int numThreads) {this.numberOfThreads = numThreads; }


    public String toString() {
        return getJobType().name() + " Bucket: " + getBucketName() + ", Created: " + Constants.DATE_FORMAT.format(getCreationDate()) + ", ID: " + getId();
    }
}
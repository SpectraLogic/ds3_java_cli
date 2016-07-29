package com.spectralogic.ds3cli.command;

import com.google.common.cache.CacheStats;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.models.DefaultResult;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.utils.Guard;

import java.text.SimpleDateFormat;

import static com.spectralogic.ds3cli.util.Utils.nullGuardToDate;
import static com.spectralogic.ds3cli.util.Utils.nullGuardToString;

public class GetConfigSummary extends CliCommand<DefaultResult> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public GetConfigSummary() {
    }

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        return this;
    }

    @Override
    public DefaultResult call() throws Exception {
        final StringBuilder result = new StringBuilder("CONFIGURATION SUMMARY\n");

        // SYSTEM INFORMATION
        final GetSystemInformationSpectraS3Response response
                = getClient().getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request());
        final com.spectralogic.ds3client.models.SystemInformation sysInfo = response.getSystemInformationResult();
        final BuildInformation buildInfo = sysInfo.getBuildInformation();
        result.append("System Information\n");
        result.append(String.format("Build Number:\t%s.%s\n", buildInfo.getVersion(), buildInfo.getRevision()));
        result.append(String.format("API Version:  \t%s\n", sysInfo.getApiVersion()));
        result.append(String.format("Serial Number: \t%s", sysInfo.getSerialNumber()));

        // DATA PATH BACKEND
        final GetDataPathBackendSpectraS3Response dataPathResponse
                = getClient().getDataPathBackendSpectraS3(new GetDataPathBackendSpectraS3Request());
        final DataPathBackend dataPathBackend = dataPathResponse.getDataPathBackendResult();
        result.append("\n\nData Path Backend");
        result.append("\nActivated                       \t");
        result.append(dataPathBackend.getActivated());
        result.append("\nAuto Timeout                    \t");
        result.append(dataPathBackend.getAutoActivateTimeoutInMins());
        result.append("\nAuto Inspect                    \t");
        result.append(dataPathBackend.getAutoInspect());
        result.append("\nConflict Resolution               \t");
        result.append(dataPathBackend.getDefaultImportConflictResolutionMode());
        result.append("\nID                                \t");
        result.append(dataPathBackend.getId());
        result.append("\nLast Heartbeat                    \t");
        result.append(dataPathBackend.getLastHeartbeat());
        result.append("\nUnavailable Media Policy          \t");
        result.append(dataPathBackend.getUnavailableMediaPolicy());
        result.append("\nUnavailable Pool Retry Mins       \t");
        result.append(dataPathBackend.getUnavailablePoolMaxJobRetryInMins());
        result.append("\nUnavailable Partition Retry Mins  \t");
        result.append(dataPathBackend.getUnavailableTapePartitionMaxJobRetryInMins());

        // GET CACHE FILESYSTEM AND INFO
        final GetCacheStateSpectraS3Response cacheStateResponse
                = getClient().getCacheStateSpectraS3(new GetCacheStateSpectraS3Request());
        final CacheInformation cacheinfo = cacheStateResponse.getCacheInformationResult();
        result.append("\n\nCache Filesystems");
        for(final CacheFilesystemInformation cacheFilesystemInfo : cacheinfo.getFilesystems()) {
            final CacheFilesystem cacheFilesystem = cacheFilesystemInfo.getCacheFilesystem();
            result.append("\n");
            result.append(cacheFilesystem.getPath());
            result.append("\nAvailable Capacity           \t");
            result.append(cacheFilesystemInfo.getAvailableCapacityInBytes());
            result.append("\nUsed Capacity                \t");
            result.append(cacheFilesystemInfo.getUsedCapacityInBytes());
            result.append("\nUnavailable Capacity           \t");
            result.append(cacheFilesystemInfo.getUnavailableCapacityInBytes());
            result.append("\nTotal Capacity                 \t");
            result.append(cacheFilesystemInfo.getTotalCapacityInBytes());
            // from the filesystem
            result.append("\n-- Max Capacity           \t");
            result.append(cacheFilesystem.getMaxCapacityInBytes());
            result.append("\n-- Aout Reclaim Threshold \t");
            result.append(cacheFilesystem.getAutoReclaimInitiateThreshold());
            result.append("\n-- Burst Threshold        \t");
            result.append(cacheFilesystem.getBurstThreshold());
            result.append("\n-- Max Utilization        \t");
            result.append(cacheFilesystem.getMaxPercentUtilizationOfFilesystem());
            // result.append("%"); really a percent? sim shows 0.9
            result.append("\n-- ID                     \t");
            result.append(cacheFilesystem.getId());
            result.append("\n-- Node ID                \t");
            result.append(cacheFilesystem.getNodeId());
        }

        // CAPACITY SUMMARY
        final GetSystemCapacitySummarySpectraS3Response systemCapacitySummaryResponse =
                getClient().getSystemCapacitySummarySpectraS3(new GetSystemCapacitySummarySpectraS3Request());
        final CapacitySummaryContainer capacitySummaryContainer =
                systemCapacitySummaryResponse.getCapacitySummaryContainerResult();
        StorageDomainCapacitySummary poolCapacity =  capacitySummaryContainer.getPool();
        StorageDomainCapacitySummary tapeCapacity = capacitySummaryContainer.getTape();

        result.append("\n\nSystem Capacity Summary\n");
        result.append("Pool Capacity:");
        result.append("\n-- Allocated\t");
        result.append(poolCapacity.getPhysicalAllocated());
        result.append("\n-- Used     \t");
        result.append(poolCapacity.getPhysicalUsed());
        result.append("\n-- Free     \t");
        result.append(poolCapacity.getPhysicalFree());
        result.append("\nTape Capacity:");
        result.append("\n-- Allocated\t");
        result.append(tapeCapacity.getPhysicalAllocated());
        result.append("\n-- Used     \t");
        result.append(tapeCapacity.getPhysicalUsed());
        result.append("\n-- Free     \t");
        result.append(tapeCapacity.getPhysicalFree());

        // SYSTEM FAILURES
        final GetSystemFailuresSpectraS3Response sysFailuresResponse
                = getClient().getSystemFailuresSpectraS3(new GetSystemFailuresSpectraS3Request());
        final SystemFailureList sysFailures = sysFailuresResponse.getSystemFailureListResult();
        if(Guard.isNullOrEmpty(sysFailures.getSystemFailures())) {
            result.append("\n\nSystem Failures: none\n");
        } else {
            for (final SystemFailure sysFailure : sysFailures.getSystemFailures()) {
                result.append("\n\nSystem Failures\n");
                result.append(sysFailure.getErrorMessage());
                result.append("\n-- Date \t");
                result.append(nullGuardToDate(sysFailure.getDate(), DATE_FORMAT));
                result.append("\n-- Type \t");
                result.append(nullGuardToString(sysFailure.getType()));
                result.append("\n-- ID   \t");
                result.append(nullGuardToString(sysFailure.getId()));
            }
        }

        return new DefaultResult(result.toString());
    }
}

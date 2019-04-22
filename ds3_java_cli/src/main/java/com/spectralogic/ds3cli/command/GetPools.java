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

package com.spectralogic.ds3cli.command;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3cli.ViewType;
import com.spectralogic.ds3cli.exceptions.BadArgumentException;
import com.spectralogic.ds3cli.models.GetPoolsResult;
import com.spectralogic.ds3cli.util.CliUtils;
import com.spectralogic.ds3cli.views.cli.GetPoolsView;
import com.spectralogic.ds3cli.views.json.DataView;
import com.spectralogic.ds3client.commands.spectrads3.GetPoolsSpectraS3Request;
import com.spectralogic.ds3client.models.PoolHealth;
import com.spectralogic.ds3client.models.PoolState;
import com.spectralogic.ds3client.models.PoolType;
import com.spectralogic.ds3client.utils.Guard;
import org.apache.commons.cli.Option;

import static com.spectralogic.ds3cli.ArgumentFactory.BUCKET;

public class GetPools extends CliCommand<GetPoolsResult> {

    private static final Option POOL_NAME = Option.builder()
            .longOpt("name")
            .hasArg(true).argName("poolName")
            .desc("Filter by pool name").build();

    public static final Option POOL_STATE = Option.builder().
            longOpt("state")
            .desc("Filter by pool state.  Possible values: [" + CliUtils.printEnumOptions(PoolState.values()) + "]")
            .hasArg(true)
            .build();

    public static final Option POOL_HEALTH = Option.builder().
            longOpt("health")
            .desc("Filter by pool health.  Possible values: [" + CliUtils.printEnumOptions(PoolHealth.values()) + "]")
            .hasArg(true)
            .build();

    public static final Option POOL_TYPE = Option.builder().
            longOpt("type")
            .desc("Filter by pool type.  Possible values: [" + CliUtils.printEnumOptions(PoolType.values()) + "]")
            .hasArg(true)
            .build();

    private final static ImmutableList<Option> optionalArgs = ImmutableList.of(POOL_NAME, BUCKET, POOL_HEALTH, POOL_STATE, POOL_TYPE);

    private String bucketName;
    private String poolName;
    private PoolState poolState;
    private PoolHealth poolHealth;
    private PoolType poolType;

    @Override
    public CliCommand init(final Arguments args) throws Exception {
        processCommandOptions(EMPTY_LIST, optionalArgs, args);

        this.bucketName = args.getBucket();
        this.poolName = args.getOptionValue(POOL_NAME.getLongOpt());

        final String stateString = args.getOptionValue(POOL_STATE.getLongOpt());
        try {
            if (!Guard.isStringNullOrEmpty(stateString)) {
                this.poolState =  PoolState.valueOf(stateString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown state: " + stateString, e);
        }

        final String healthString = args.getOptionValue(POOL_HEALTH.getLongOpt());
        try {
            if (!Guard.isStringNullOrEmpty(healthString)) {
                this.poolHealth =  PoolHealth.valueOf(healthString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown pool health: " + healthString, e);
        }

        final String typeString = args.getOptionValue(POOL_TYPE.getLongOpt());
        try {
            if (!Guard.isStringNullOrEmpty(typeString)) {
                this.poolType =  PoolType.valueOf(typeString.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            throw new BadArgumentException("Unknown pool type: " + typeString, e);
        }
        return this;
    }


    @Override
    public GetPoolsResult call() throws Exception {
        final GetPoolsSpectraS3Request request = new GetPoolsSpectraS3Request();
        if(!Guard.isStringNullOrEmpty(bucketName)) {
            request.withBucketId(bucketName);
        }
        if(!Guard.isStringNullOrEmpty(poolName)) {
            request.withName(poolName);
        }
        if (poolState != null) {
            request.withState(poolState);
        }
        if (poolHealth != null) {
            request.withHealth(poolHealth);
        }
        if (poolType != null) {
            request.withType(poolType);
        }

        return new GetPoolsResult(getClient().getPoolsSpectraS3(request).getPoolListResult());
    }

     @Override
    public View<GetPoolsResult> getView() {
        if (viewType == ViewType.JSON) {
            return new DataView<>();
        }
        return new GetPoolsView();
    }
}

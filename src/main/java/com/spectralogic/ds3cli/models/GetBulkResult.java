package com.spectralogic.ds3cli.models;

public class GetBulkResult {
    final private String result;

    final public String getResult(){
        return this.result;
    }

    public GetBulkResult(final String result){
        this.result = result;
    }
}

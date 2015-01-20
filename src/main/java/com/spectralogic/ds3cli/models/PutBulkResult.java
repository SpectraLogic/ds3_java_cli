package com.spectralogic.ds3cli.models;

public class PutBulkResult {
    final private String result;

    final public String getResult(){
        return this.result;
    }

    public PutBulkResult(final String result){
        this.result = result;
    }
}

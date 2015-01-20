package com.spectralogic.ds3cli.models;

public class PutBucketResult {
    final private String result;

    final public String getResult(){
        return this.result;
    }

    public PutBucketResult(final String result){
        this.result = result;
    }
}

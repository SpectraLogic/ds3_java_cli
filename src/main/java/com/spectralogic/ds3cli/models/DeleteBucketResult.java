package com.spectralogic.ds3cli.models;

public class DeleteBucketResult {
    final private String result;

    final public String getResult(){
        return this.result;
    }

    public DeleteBucketResult(final String result){
        this.result = result;
    }
}

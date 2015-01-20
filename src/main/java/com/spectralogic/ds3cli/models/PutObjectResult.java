package com.spectralogic.ds3cli.models;

public class PutObjectResult {
    final private String result;

    final public String getResult(){
        return this.result;
    }

    public PutObjectResult(final String result){
        this.result = result;
    }
}

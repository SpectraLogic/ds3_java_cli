package com.spectralogic.ds3cli.models;

public class DeleteObjectResult {
    final private String result;

    final public String getResult(){
        return this.result;
    }

    public DeleteObjectResult(final String result){
        this.result = result;
    }
}

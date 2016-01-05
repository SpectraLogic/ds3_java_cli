package com.spectralogic.ds3cli.models;

public class PerformanceResult implements Result{
    final private String result;

    public PerformanceResult(final String result) { this.result = result; }

    final public String getResult() { return this.result; }
}

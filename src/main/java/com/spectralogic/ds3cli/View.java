package com.spectralogic.ds3cli;

public interface View<T>{

    public String render(final T obj);
}

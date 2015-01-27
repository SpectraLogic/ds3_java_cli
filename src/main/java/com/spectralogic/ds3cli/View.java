package com.spectralogic.ds3cli;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface View<T>{

    public String render(final T obj) throws JsonProcessingException;
}

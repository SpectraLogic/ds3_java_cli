package com.spectralogic.ds3cli.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import java.io.IOException;

public final class JsonMapper {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper().registerModule(new GuavaModule());
    }

    private JsonMapper() {}

    public static<T> T toModel(final String jsonBody, final Class<T> clazz) throws IOException {
        return mapper.readValue(jsonBody, clazz);
    }
}

package com.spectralogic.ds3cli.integration.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonMapper() {}

    public static<T> T toModel(final String jsonBody, final Class<T> clazz) throws IOException {
        return mapper.readValue(jsonBody, clazz);
    }


}

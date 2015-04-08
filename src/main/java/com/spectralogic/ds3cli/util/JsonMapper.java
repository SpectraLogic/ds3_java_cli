package com.spectralogic.ds3cli.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonMapper {
    private final static ObjectWriter writer;
    private JsonMapper() {
    }

    static {
        final ObjectMapper mapper = new ObjectMapper();
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    public static String toJson(final Object obj) throws JsonProcessingException {
        return writer.writeValueAsString(obj);
    }
}

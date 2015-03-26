package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3client.networking.FailedRequestException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CommandExceptionJsonView implements View<CommandException> {
    @Override
    public String render(final CommandException obj) throws JsonProcessingException {
        final Map<String, String> jsonBackingMap = new HashMap<>();

        jsonBackingMap.put("message", obj.getMessage());

        try {
            final ObjectMapper mapper = new ObjectMapper();
            if (obj.getCause() != null) {
                if (obj.getCause() instanceof FailedRequestException) {
                    final FailedRequestException ce = (FailedRequestException) obj.getCause();
                    jsonBackingMap.put("status_code", Integer.toString(ce.getStatusCode()));
                    jsonBackingMap.put("api_error_message", ce.getResponseString());
                } else {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final PrintWriter pOut = new PrintWriter(out);
                    obj.printStackTrace(pOut);
                    try {
                        jsonBackingMap.put("stack_trace", out.toString("utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonBackingMap);
        }
        catch(final Exception e) {
            return "Failed to render error: " + e.getMessage();
        }
    }
}

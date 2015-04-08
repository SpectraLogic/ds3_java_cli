package com.spectralogic.ds3cli.views.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectralogic.ds3cli.CommandException;
import com.spectralogic.ds3cli.View;
import com.spectralogic.ds3client.networking.FailedRequestException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;
import java.util.Map;

public class CommandExceptionJsonView implements View<CommandException> {
    @Override
    public String render(final CommandException obj) throws JsonProcessingException {
        final CommonJsonView view = CommonJsonView.newView(CommonJsonView.Status.ERROR);
        final Map<String, String> jsonBackingMap = new TreeMap<>();

        view.message(obj.getMessage());

        try {
            final ObjectMapper mapper = new ObjectMapper();
            if (obj.getCause() != null) {
                if (obj.getCause() instanceof FailedRequestException) {
                    final FailedRequestException ce = (FailedRequestException) obj.getCause();
                    jsonBackingMap.put("StatusCode", Integer.toString(ce.getStatusCode()));
                    jsonBackingMap.put("ApiErrorMessage", ce.getResponseString());
                } else {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final PrintWriter pOut = new PrintWriter(out);
                    obj.printStackTrace(pOut);
                    try {
                        jsonBackingMap.put("StackTrace", out.toString("utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(view.data(jsonBackingMap));
        }
        catch(final Exception e) {
            return "Failed to render error: " + e.getMessage();
        }
    }
}

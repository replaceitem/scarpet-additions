package net.replaceitem.scarpet.additions;

import carpet.script.exception.InternalExpressionException;
import carpet.script.exception.ThrowStatement;
import carpet.script.value.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtils {

    public static HttpClient client = HttpClient.newBuilder().build();

    public static Value httpRequest(Map<Value,Value> options) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();

        String uri = getOption(options, "uri", true).getString();
        builder.uri(URI.create(uri));

        Value methodValue = getOption(options, "method", false);
        String method = methodValue == null ? "GET" : methodValue.getString();

        Value bodyValue = getOption(options, "body", false);
        HttpRequest.BodyPublisher bodyPublisher;
        if(bodyValue == null) {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyValue.getString());
        }
        builder.method(method, bodyPublisher);

        Value headerValue = getOption(options, "headers", false);
        if(headerValue != null) {
            if(!(headerValue instanceof MapValue headerMapValue)) throw new InternalExpressionException("'headers' needs to be a map");
            Map<Value, Value> header = headerMapValue.getMap();
            for (Map.Entry<Value, Value> entry : header.entrySet()) {
                String keyString = entry.getKey().getString();
                Value value = entry.getValue();
                if (value instanceof ListValue listValue) {
                    for (Value listVal : listValue) {
                        builder.header(keyString, listVal.getString());
                    }
                } else {
                    builder.header(keyString, value.getString());
                }
            }
        }

        HttpRequest request = builder.build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            ScarpetAdditions.LOGGER.error("Error sending http request", e);
            throw new ThrowStatement("Error sending http request: " + e.getMessage(), ScarpetAdditions.HTTP_REQUEST_ERROR);
        }

        Map<Value, Value> responseMap = new HashMap<>();
        responseMap.put(StringValue.of("status_code"), NumericValue.of(response.statusCode()));
        responseMap.put(StringValue.of("body"), StringValue.of(response.body()));

        Set<Map.Entry<String, List<String>>> headerEntries = response.headers().map().entrySet();
        Map<Value, Value> headersValueMap = new HashMap<>();
        for (Map.Entry<String, List<String>> headerEntry : headerEntries) {
            headersValueMap.put(StringValue.of(headerEntry.getKey()), ListValue.wrap(headerEntry.getValue().stream().map(StringValue::of)));
        }
        responseMap.put(StringValue.of("headers"), MapValue.wrap(headersValueMap));
        responseMap.put(StringValue.of("uri"), StringValue.of(response.uri().toString()));

        return MapValue.wrap(responseMap);
    }

    private static Value getOption(Map<Value,Value> options, String key, boolean required) {
        Value value = options.get(StringValue.of(key));
        if(required && value == null) throw new InternalExpressionException("Missing '" + key + "' value for http request");
        return value;
    }
}

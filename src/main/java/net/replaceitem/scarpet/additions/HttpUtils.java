package net.replaceitem.scarpet.additions;

import carpet.script.api.Auxiliary;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.*;
import com.google.gson.*;
import org.apache.commons.text.StringEscapeUtils;
import org.checkerframework.common.value.qual.StringVal;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HttpUtils {

    public static HttpClient client = null;

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
            if(!(headerValue instanceof MapValue headerMapValue)) throw new InternalExpressionException("'header' needs to be a map");
            Map<Value, Value> header = headerMapValue.getMap();
            header.forEach((key, mapVal) -> {
                String keyString = key.getString();
                if(mapVal instanceof ListValue listValue) {
                    for (Value listVal : listValue) {
                        builder.header(keyString, listVal.getString());
                    }
                } else {
                    builder.header(keyString, mapVal.getString());
                }
            });
        }

        HttpRequest request = builder.build();

        if(client == null) client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new InternalExpressionException("Error sending http request: " + e.getMessage());
        }

        Map<Value, Value> responseMap = new HashMap<>();
        responseMap.put(StringValue.of("statusCode"), NumericValue.of(response.statusCode()));
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

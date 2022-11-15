package net.replaceitem.scarpet.additions;

import carpet.script.api.Auxiliary;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.MapValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import com.google.gson.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    public static HttpClient client = null;


    private static JsonElement escapeHtml(JsonElement element) {
        if(element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            object.entrySet().forEach(stringJsonElementEntry -> stringJsonElementEntry.setValue(escapeHtml(stringJsonElementEntry.getValue())));
            return object;
        }
        if(element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            array.forEach(jsonElement -> jsonElement = escapeHtml(jsonElement));
            return array;
        }
        if(element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if(primitive.isString()) {
                return new JsonPrimitive(StringEscapeUtils.unescapeHtml4(primitive.getAsString()));
            }
        }
        return element;
    }

    private static Value parseResponse(BufferedReader reader) throws JsonParseException {
        JsonElement json = JsonParser.parseReader(reader);
        json = escapeHtml(json);
        Value response = Auxiliary.GSON.fromJson(json, Value.class);
        return response == null ? Value.FALSE : response;
    }

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

        Value headerValue = getOption(options, "header", false);
        if(headerValue != null) {
            if(!(headerValue instanceof MapValue headerMapValue)) throw new InternalExpressionException("'header' needs to be a map");
            Map<Value, Value> header = headerMapValue.getMap();
            header.forEach((key, val) -> builder.header(key.getString(),val.getString()));
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
        responseMap.put(StringValue.of("headers"), StringValue.of(response.headers().toString()));
        responseMap.put(StringValue.of("uri"), StringValue.of(response.uri().toString()));

        return MapValue.wrap(responseMap);
    }

    private static Value getOption(Map<Value,Value> options, String key, boolean required) {
        Value value = options.get(StringValue.of(key));
        if(required && value == null) throw new InternalExpressionException("Missing '" + key + "' value for http request");
        return value;
    }
}

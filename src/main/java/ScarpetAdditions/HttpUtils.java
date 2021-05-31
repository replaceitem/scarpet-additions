package ScarpetAdditions;

import carpet.script.api.Auxiliary;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.Value;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;

public class HttpUtils {
    private static final JsonParser jsonParser = new JsonParser();

    public static HttpURLConnection openConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }

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
        JsonElement json = jsonParser.parse(reader);
        json = escapeHtml(json);
        return Auxiliary.GSON.fromJson(json, Value.class);
    }

    public static Value httpGet(String url) {
        Value response;
        try {
            HttpURLConnection connection = openConnection(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = parseResponse(reader);
            connection.disconnect();
        } catch (JsonParseException | IOException | IllegalArgumentException e) {
            ScarpetAdditions.LOGGER.error("html_get error: " + e);
            return Value.NULL;
        }
        return response;
    }
}

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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;

public class HttpUtils {
    private static final JsonParser jsonParser = new JsonParser();

    public static HttpURLConnection openConnection(String requestMethod, String urlString, int connectTimeout, int readTimeout) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
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

    public static Value httpRequest(String requestMethod, String body, String url, int connectTimeout, int readTimeout) {
        Value response;
        try {
            HttpURLConnection connection = openConnection(requestMethod.toUpperCase(), url, connectTimeout, readTimeout);
            if(body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length",Integer.toString(body.length()));
                DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                stream.writeBytes(body);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = parseResponse(reader);
            connection.disconnect();
        } catch (JsonParseException | IOException | IllegalArgumentException e) {
            ScarpetAdditions.LOGGER.error("html request error: " + e);
            return Value.NULL;
        }
        return response;
    }
}

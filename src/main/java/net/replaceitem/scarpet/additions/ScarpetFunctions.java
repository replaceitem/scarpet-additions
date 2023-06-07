package net.replaceitem.scarpet.additions;

import carpet.script.CarpetContext;
import carpet.script.Context;
import carpet.script.annotation.ScarpetFunction;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.EntityValue;
import carpet.script.value.FormattedTextValue;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.text.StringEscapeUtils;

import java.awt.*;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ScarpetFunctions {
    
    @ScarpetFunction
    public void set_motd(Context c, Value text) {
        if(text == null || text.isNull()) {
            ScarpetAdditions.MOTD = null;
        } else {
            ScarpetAdditions.MOTD = FormattedTextValue.getTextByValue(text);
        }
    }


    @ScarpetFunction
    public Value convert_color(Value input, String model, String out) {
        if(!(input instanceof ListValue)) throw new InternalExpressionException("'convert_color' requires a List as the first argument");

        List<Value> col = ((ListValue) input).getItems();

        Color color;

        if(model.equalsIgnoreCase("RGB")) {
            if(col.size() == 3) {
                float v1 = (float)(col.get(0) instanceof NumericValue?((NumericValue) col.get(0)).getInt():0);
                float v2 = (float)(col.get(1) instanceof NumericValue?((NumericValue) col.get(1)).getInt():0);
                float v3 = (float)(col.get(2) instanceof NumericValue?((NumericValue) col.get(2)).getInt():0);

                color = new Color(v1/255,v2/255,v3/255);
            } else
                throw new InternalExpressionException("Color model " + model + " needs a List of three values as the first argument");
        } else if (model.equalsIgnoreCase("RGBA")) {
            if(col.size() == 4) {
                float v1 = (float)(col.get(0) instanceof NumericValue?((NumericValue) col.get(0)).getInt():0);
                float v2 = (float)(col.get(1) instanceof NumericValue?((NumericValue) col.get(1)).getInt():0);
                float v3 = (float)(col.get(2) instanceof NumericValue?((NumericValue) col.get(2)).getInt():0);
                float v4 = (float)(col.get(3) instanceof NumericValue?((NumericValue) col.get(3)).getInt():0);

                color = new Color(v1/255,v2/255,v3/255,v4/255);
            } else
                throw new InternalExpressionException("Color model " + model + " needs a List of four values as the first argument");
        } else if (model.equalsIgnoreCase("HSB")) {
            if(col.size() == 3) {
                float v1 = (float) (col.get(0) instanceof NumericValue ? ((NumericValue) col.get(0)).getInt() : 0);
                float v2 = (float) (col.get(1) instanceof NumericValue ? ((NumericValue) col.get(1)).getInt() : 0);
                float v3 = (float) (col.get(2) instanceof NumericValue ? ((NumericValue) col.get(2)).getInt() : 0);

                color = Color.getHSBColor(v1 / 360, v2 / 255, v3 / 255);
            } else {
                throw new InternalExpressionException("Color model " + model + " needs a List of three values as the first argument");
            }
        } else {
            throw new InternalExpressionException("Invalid input color model " + model);
        }

        if(out.equalsIgnoreCase("RGB")) {
            return ListValue.of(NumericValue.of(color.getRed()),NumericValue.of(color.getGreen()),NumericValue.of(color.getBlue()));
        } else if(out.equalsIgnoreCase("RGBA")) {
            return ListValue.of(NumericValue.of(color.getRed()),NumericValue.of(color.getGreen()),NumericValue.of(color.getBlue()),NumericValue.of(color.getAlpha()));
        } else if(out.equalsIgnoreCase("NUM")) {
            return NumericValue.of((((long)(color.getRGB() & 0xFFFFFF))<<8) | (color.getAlpha()));
        } else if(out.equalsIgnoreCase("HEX")) {
            return StringValue.of(String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        } else {
            throw new InternalExpressionException("Invalid output color model " + model);
        }
    }

    @ScarpetFunction
    public Value http_request(Map<Value,Value> options) {
        return HttpUtils.httpRequest(options);
    }

    @ScarpetFunction
    public String escape_html(String html) {
        return StringEscapeUtils.escapeHtml4(html);
    }

    @ScarpetFunction
    public String unescape_html(String html) {
        return StringEscapeUtils.unescapeHtml4(html);
    }
    
    @ScarpetFunction(maxParams = 3)
    public void list_text(Context c, Value headerValue, Value footerValue, Value... optionalPlayer) {
        Text header = FormattedTextValue.getTextByValue(headerValue);
        Text footer = FormattedTextValue.getTextByValue(footerValue);
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(header, footer);
        MinecraftServer server = ((CarpetContext) c).source().getServer();
        ServerPlayerEntity player = optionalPlayer.length == 1 ? EntityValue.getPlayerByValue(server, optionalPlayer[0]) : null;
        if (player == null) {
            server.getPlayerManager().sendToAll(packet);
        } else {
            player.networkHandler.sendPacket(packet);
        }
    }
}

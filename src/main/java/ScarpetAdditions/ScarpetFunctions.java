package ScarpetAdditions;

import carpet.script.CarpetContext;
import carpet.script.Context;
import carpet.script.Expression;
import carpet.script.argument.FunctionArgument;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.EntityValue;
import carpet.script.value.FormattedTextValue;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ScarpetFunctions {
    public static void apply(Expression expr) {
        expr.addContextFunction("set_motd", 1, (c, t, lv) -> {
            Value motdValue = lv.get(0);
            Text motd;
            if (motdValue instanceof FormattedTextValue) {
                motd = ((FormattedTextValue) motdValue).getText();
            } else {
                motd = new LiteralText(motdValue.getString());
            }
            ((CarpetContext) c).s.getServer().getServerMetadata().setDescription(motd);
            return Value.TRUE;
        });

        expr.addContextFunction("convert_color", 3, (c, t, lv) -> {
            Value input = lv.get(0);
            String model = lv.get(1).getString();
            String out = lv.get(2).getString();

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
        });

        expr.addContextFunction("http", -1, (c, t, lv) -> {
            if(lv.size() < 5 || lv.size() > 6) throw new InternalExpressionException("'http' requires 3 or 4 arguments");
            String requestMethod = lv.get(0).getString();
            String urlString = lv.get(1).getString();
            int connectTimeout = NumericValue.asNumber(lv.get(2)).getInt();
            int readTimeout = NumericValue.asNumber(lv.get(3)).getInt();

            final String body = lv.size()==6?lv.get(4).getString():null;

            FunctionArgument functionArgument = FunctionArgument.findIn(c, expr.module, lv, lv.size()==6?5:4, false, false);

            new Thread(() -> {
                Value response;
                response = HttpUtils.httpRequest(requestMethod,body,urlString,connectTimeout,readTimeout);
                functionArgument.function.callInContext(c, Context.Type.NONE, Collections.singletonList(response));
            }).start();

            return Value.TRUE;
        });


        expr.addContextFunction("list_text",-1, (c, t, lv) -> {
            if(lv.size() < 2 || lv.size() > 3) throw new InternalExpressionException("list_header requires 2 or 3 parameters");
            Text header = FormattedTextValue.getTextByValue(lv.get(0));
            Text footer = FormattedTextValue.getTextByValue(lv.get(1));
            PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(header,footer);
            if(lv.size() == 3) {
                EntityValue.getPlayerByValue(((CarpetContext) c).s.getServer(),lv.get(2)).networkHandler.sendPacket(packet);
            } else {
                ((CarpetContext)c).s.getServer().getPlayerManager().sendToAll(packet);
            }
            return Value.TRUE;
        });
    }
}

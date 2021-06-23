package ScarpetAdditions;

import carpet.script.CarpetContext;
import carpet.script.Context;
import carpet.script.Expression;
import carpet.script.argument.FunctionArgument;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.EntityValue;
import carpet.script.value.FormattedTextValue;
import carpet.script.value.ListValue;
import carpet.script.value.NBTSerializableValue;
import carpet.script.value.NullValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import carpet.script.value.ValueConversions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
            ((CarpetContext) c).s.getMinecraftServer().getServerMetadata().setDescription(motd);
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

        expr.addContextFunction("virtual_inventory", -1, (c, t, lv) -> {
            if (lv.size() == 0) {
                return ListValue.wrap(ScarpetAdditions.virtualInventories.keySet().stream().map(StringValue::of).collect(Collectors.toList()));
            }

            if (lv.size() == 1) {
                String key = lv.get(0).getString();
                SimpleInventory inv = ScarpetAdditions.virtualInventories.get(key);
                if (inv == null) throw new InternalExpressionException("Unknown virtual inventory");
                ArrayList<Value> items = new ArrayList<>();
                for (int i = 0; i < inv.size(); i++) {
                    items.add(ValueConversions.of(inv.getStack(i)));
                }
                return ListValue.wrap(items);
            }

            if (lv.size() == 2) {
                String key = lv.get(0).getString();
                SimpleInventory inv = ScarpetAdditions.virtualInventories.get(key);
                Value itemList = lv.get(1);
                if (itemList instanceof NumericValue) {
                    if (itemList instanceof NullValue) {
                        ScarpetAdditions.virtualInventories.remove(key);
                        return Value.TRUE;
                    }
                    ScarpetAdditions.virtualInventories.put(key, new SimpleInventory(((NumericValue) itemList).getInt() * 9));
                    return Value.TRUE;
                }
                if (!(itemList instanceof ListValue))
                    throw new InternalExpressionException("Need a List of items as 2nd argument");
                List<Value> items = ((ListValue) itemList).getItems();
                for (int i = 0; i < Math.min(inv.size(), items.size()); i++) {
                    if (!(items.get(i) instanceof ListValue)) continue;
                    List<Value> item = ((ListValue) items.get(i)).getItems();
                    NbtCompound nbt;
                    Value nbtValue = item.get(2);
                    int count = (int) NumericValue.asNumber(item.get(1)).getLong();
                    if (nbtValue instanceof NBTSerializableValue) {
                        nbt = ((NBTSerializableValue) nbtValue).getCompoundTag();
                    } else if (nbtValue instanceof NullValue) {
                        nbt = null;
                    } else {
                        nbt = (new NBTSerializableValue(nbtValue.getString())).getCompoundTag();
                    }
                    ItemStackArgument newitem = NBTSerializableValue.parseItem(item.get(0).getString(), nbt);
                    try {
                        inv.setStack(i, newitem.createStack(count, false));
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                return ListValue.wrap(items);
            }
            throw new InternalExpressionException("'virtual_inventory' requires zero to two arguments");
        });


        expr.addContextFunction("open_inventory", 3, (c, t, lv) -> {
            Value playerValue = (lv.get(0));
            PlayerEntity player;

            if (playerValue instanceof EntityValue) {
                Entity entity = ((EntityValue) playerValue).getEntity();
                if (entity instanceof PlayerEntity) {
                    player = ((PlayerEntity) entity);
                } else {
                    return Value.FALSE;
                }
            } else {
                return Value.FALSE;
            }


            Text inventoryName = new LiteralText((lv.get(1)).getString());

            SimpleInventory inv = ScarpetAdditions.virtualInventories.get(lv.get(2).getString());

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
                int rows = (int) Math.ceil(((double) inv.size()) / 9);
                ScreenHandlerType<GenericContainerScreenHandler> handlerType = getScreenHandlerTypeFromRowCount(rows);
                if (handlerType == null)
                    throw new InternalExpressionException("Invalid inventory size, must be max 54");
                return new GenericContainerScreenHandler(handlerType, i, playerInventory, inv, rows);
            }, inventoryName));

            return Value.TRUE;
        });

        expr.addContextFunction("http", 3, (c, t, lv) -> {
            String requestMethod = lv.get(0).getString();
            String urlString = lv.get(1).getString();

            FunctionArgument functionArgument = FunctionArgument.findIn(c, expr.module, lv, 2, false, false);

            new Thread(() -> {
                Value response;
                response = HttpUtils.httpRequest(requestMethod,urlString);
                functionArgument.function.callInContext(c, Context.Type.NONE, Collections.singletonList(response));
            }).start();

            return Value.TRUE;
        });
    }

    static ScreenHandlerType<GenericContainerScreenHandler> getScreenHandlerTypeFromRowCount(int rows) {
        switch (rows) {
            case 1:
                return ScreenHandlerType.GENERIC_9X1;
            case 2:
                return ScreenHandlerType.GENERIC_9X2;
            case 3:
                return ScreenHandlerType.GENERIC_9X3;
            case 4:
                return ScreenHandlerType.GENERIC_9X4;
            case 5:
                return ScreenHandlerType.GENERIC_9X5;
            case 6:
                return ScreenHandlerType.GENERIC_9X6;
            default:
                return null;
        }
    }
}

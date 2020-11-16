package ScarpetAdditions;

import carpet.script.Expression;
import carpet.script.LazyValue;
import carpet.script.value.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

import static ScarpetAdditions.HTTPGetMethod.getHTML;

public class ScarpetFunctions {
    public static void apply(Expression expr) {
        expr.addLazyFunction("set_tab_text", 2, (c, t, lv) -> {
            Value ret = Value.FALSE;
            Value head = lv.get(0).evalValue(c);
            Value foot = lv.get(1).evalValue(c);

            if(head instanceof FormattedTextValue) {
                ScarpetAdditions.customHeader = (LiteralText) ((FormattedTextValue) head).getText();
                ScarpetAdditions.updateTabHeader = true;
                ret = Value.TRUE;
            } else if (head instanceof StringValue) {
                ScarpetAdditions.customHeader = new LiteralText(((StringValue) head).getString());
                ScarpetAdditions.updateTabHeader = true;
                ret = Value.TRUE;
            } else {
                if(head == Value.NULL) {
                    ScarpetAdditions.customHeader = new LiteralText("");
                    ScarpetAdditions.updateTabHeader = true;
                    ret = Value.TRUE;
                }
            }

            if(foot instanceof FormattedTextValue) {
                ScarpetAdditions.customFooter = (LiteralText) ((FormattedTextValue) foot).getText();
                ScarpetAdditions.updateTabHeader = true;
                ret = Value.TRUE;
            } else if (foot instanceof StringValue) {
                ScarpetAdditions.customFooter = new LiteralText(((StringValue) foot).getString());
                ScarpetAdditions.updateTabHeader = true;
                ret = Value.TRUE;
            } else {
                if(foot == Value.NULL) {
                    ScarpetAdditions.customFooter = new LiteralText("");
                    ScarpetAdditions.updateTabHeader = true;
                    ret = Value.TRUE;
                }
            }


            final Value fret = ret;
            return (cc, tt) -> fret;
        });

        expr.addLazyFunction("convert_color", 4, (c, t, lv) -> {
            int v1 = NumericValue.asNumber(((LazyValue)lv.get(0)).evalValue(c)).getInt();
            int v2 = NumericValue.asNumber(((LazyValue)lv.get(1)).evalValue(c)).getInt();
            int v3 = NumericValue.asNumber(((LazyValue)lv.get(2)).evalValue(c)).getInt();
            String model = lv.get(3).evalValue(c).getString();

            String hex;

            if(model.equalsIgnoreCase("RGB")) {
                /*
                hex = Integer.toHexString(
                        ((v1 & 0xFF) << 16) |
                        ((v2 & 0xFF) << 8)  |
                        ((v3 & 0xFF) << 0)
                );*/
                hex = String.format("#%02X%02X%02X",v1, v2, v3);
            } else if (model.equalsIgnoreCase("HSB")) {
                int rgb = Color.HSBtoRGB(((float)v1)/359,((float)v2)/255,((float)v3)/255);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                hex = String.format("#%02X%02X%02X",r, g, b);
            } else {
                return (cc, tt) -> {
                    return Value.NULL;
                };
            }
            return (cc, tt) -> new StringValue(hex);
        });


        expr.addLazyFunction("http_get", 1, (c, t, lv) -> {
            String url = (lv.get(0)).evalValue(c).getString();
            String ret;
            try {
                ret = getHTML(url);
            } catch (Exception e) {
                e.printStackTrace();
                return (cc, tt) -> {
                    return Value.FALSE;
                };
            }
            return (cc, tt) -> new StringValue(ret);
        });

        expr.addLazyFunction("open_inventory", 3, (c, t, lv) -> {
            Value playerValue = (lv.get(0)).evalValue(c);
            Value inventoryValue = (lv.get(2)).evalValue(c);
            Text inventoryName = new LiteralText((lv.get(1)).evalValue(c).getString());
            PlayerEntity player;
            if(playerValue instanceof EntityValue) {
                Entity entity = ((EntityValue)playerValue).getEntity();
                if(entity instanceof PlayerEntity) {
                    player = ((PlayerEntity)entity);
                } else {
                    return (cc, tt) -> {
                        return Value.FALSE;
                    };
                }
            } else {
                return (cc, tt) -> {
                    return Value.FALSE;
                };
            }

            SimpleInventory inv = new SimpleInventory(27);
            if(inventoryValue instanceof ListValue) {
                List<Value> inventoryList = ((ListValue)inventoryValue).getItems();
                for(int i = 0; i < Math.min(inventoryList.size(),inv.size()); i++) {
                    Value itemList = inventoryList.get(i);
                    if(itemList instanceof ListValue) {
                        List<Value> item = ((ListValue) itemList).getItems();
                        if (item.size() == 3) {
                            CompoundTag nbt = null;
                            Value nbtValue = item.get(2);
                            int count = (int)NumericValue.asNumber(((NumericValue)(item.get(1)))).getLong();
                            if (nbtValue instanceof NBTSerializableValue) {
                                nbt = ((NBTSerializableValue)nbtValue).getCompoundTag();
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
                    }
                }
            }
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
                return GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, inv);
            }, inventoryName));

            return (cc, tt) -> Value.TRUE;
        });
    }
}

package discarpet;

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

import static discarpet.HTTPGetMethod.getHTML;

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
            return (cc, tt) -> {
                return fret;
            };
        });

        expr.addLazyFunction("color", 3, (c, t, lv) -> {
            float hue = ((float)(NumericValue.asNumber(((LazyValue)lv.get(0)).evalValue(c)).getInt()))/360;
            float sat = ((float)(NumericValue.asNumber(((LazyValue)lv.get(1)).evalValue(c)).getInt()))/255;
            float bri = ((float)(NumericValue.asNumber(((LazyValue)lv.get(2)).evalValue(c)).getInt()))/255;

            int rgb = Color.HSBtoRGB(hue, sat, bri);
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            String hex = String.format("#%02X%02X%02X",red, green, blue);
            //String hex = String.valueOf(red) + " " + String.valueOf(green) + " " + String.valueOf(blue);
            return (cc, tt) -> {
                return new StringValue(hex);
            };
        });


        expr.addLazyFunction("fetch", 2, (c, t, lv) -> {
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
            return (cc, tt) -> {
                return new StringValue(ret);
            };
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
                for(int i = 0; i < inventoryList.size(); i++) {
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

            return (cc, tt) -> {
                return Value.TRUE;
            };
        });
    }
}

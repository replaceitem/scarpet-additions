package ScarpetAdditions;

import carpet.script.CarpetContext;
import carpet.script.Expression;
import carpet.script.LazyValue;
import carpet.script.argument.BlockArgument;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        expr.addLazyFunction("virtual_inventory", -1, (c, t, lv) -> {
            if(lv.size() == 0) {
                return (cc,tt) -> ListValue.wrap(ScarpetAdditions.virtualInventories.keySet().stream().map(StringValue::of).collect(Collectors.toList()));
            }

            if(lv.size() == 1) {
                String key = lv.get(0).evalValue(c).getString();
                SimpleInventory inv = ScarpetAdditions.virtualInventories.get(key);
                if(inv == null) throw new InternalExpressionException("Unknown virtual inventory");
                ArrayList<Value> items = new ArrayList<>();
                for(int i = 0; i < inv.size(); i++) {
                    items.add(ValueConversions.of(inv.getStack(i)));
                }
                return (cc,tt) -> ListValue.wrap(items);
            }

            if(lv.size() == 2) {
                String key = lv.get(0).evalValue(c).getString();
                SimpleInventory inv = ScarpetAdditions.virtualInventories.get(key);
                Value itemList = lv.get(1).evalValue(c);
                if(itemList instanceof NumericValue || itemList instanceof NullValue) {
                    if(itemList instanceof NullValue) {
                        ScarpetAdditions.virtualInventories.remove(key);
                        return LazyValue.TRUE;
                    }
                    ScarpetAdditions.virtualInventories.put(key,new SimpleInventory(((NumericValue) itemList).getInt()*9));
                    return LazyValue.TRUE;
                }
                if(!(itemList instanceof ListValue)) throw new InternalExpressionException("Need a List of items as 2nd argument");
                List<Value> items = ((ListValue) itemList).getItems();
                for(int i = 0; i < Math.min(inv.size(),items.size()); i++) {
                    if(!(items.get(i) instanceof ListValue)) continue;
                    List<Value> item = ((ListValue) items.get(i)).getItems();
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
                return (cc,tt) -> ListValue.wrap(items);
            }
            throw new InternalExpressionException("'virtual_inventory' requires zero to two arguments");
        });


        expr.addLazyFunction("open_inventory", 3, (c, t, lv) -> {
            Value playerValue = (lv.get(0)).evalValue(c);
            PlayerEntity player;

            if(playerValue instanceof EntityValue) {
                Entity entity = ((EntityValue)playerValue).getEntity();
                if(entity instanceof PlayerEntity) {
                    player = ((PlayerEntity)entity);
                } else {
                    return LazyValue.FALSE;
                }
            } else {
                return LazyValue.FALSE;
            }


            Text inventoryName = new LiteralText((lv.get(1)).evalValue(c).getString());

            SimpleInventory inv = ScarpetAdditions.virtualInventories.get(lv.get(2).evalValue(c).getString());

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
                int rows = (int) Math.ceil(((double)inv.size())/9);
                ScreenHandlerType handlerType = getScreenHandlerTypeFromRowCount(rows);
                if(handlerType == null) throw new InternalExpressionException("Invalid inventory size, must be max 54");
                return new GenericContainerScreenHandler(handlerType, i, playerInventory, inv, rows);
            },inventoryName));

            return (cc, tt) -> Value.TRUE;
        });
    }

    static ScreenHandlerType<GenericContainerScreenHandler> getScreenHandlerTypeFromRowCount(int rows) {
        switch(rows) {
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

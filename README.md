# scarpet-additions

![scarpet-additions](src/main/resources/assets/scarpet-additions/icon.png)

## A Carpet extension for some additional scarpet functions

Message me on discord: replaceitem#9118

## Functions

### `set_tab_text(header,footer)`

`header` -> String OR formattedText: Text to set for header

`footer` -> String OR formattedText: Text to set for footer

Note: Carpet's loggers needs to be off, otherwise it would conflict

Example: `set_tab_text(format('r Red header'),format('#FF4411 Some custom hex color header'));`

Note that this will get reset when the player rejoins the game, so change it regularly with a script or attach it to the player join event.

&nbsp;&nbsp;

### `convert_color(a,b,c,model)`

Converts a color to hexadecimal color.

Especially usefull for example for formatting hue shifting text (see last example).

`a` -> Number: Red value (RGB, 0-255) or hue (HSB, 0-360)

`b` -> Number: green value (RGB, 0-255) or saturation (HSB, 0-255)

`c` -> Number: blue value (RGB, 0-255) or brightness (HSB, 0-255)

`model` -> String: Either `RGB` for RGB color input or `HSB` for  HSB color input

Examples:

`convert_color(255,128,0,'rgb');` -> `'#FF8000'`

`convert_color(0,255,255,'hsb');` -> `'FF0000'`

`convert_color(120,255,255,'hsb');` -> `'00FF00'`

```py
__on_tick() -> (
    if((tick_time() % 2) == 0,
        headerHue = tick_time()%360;
        headerGlossIndex = (floor(tick_time()/3)%40)-10;
        header = [];
        title = 'MinecraftServer';
        for(range(length(title)),
            if(abs(_-headerGlossIndex) < 3,
                c = color(headerHue,abs(_-headerGlossIndex)/3*255,255);
            ,
                if(_ < 7,
                    c = color(headerHue,255,190);
                ,
                    c = color(headerHue,255,255);
                );
            );
            put(header,null,str('b%s %s',c,slice(title,_,_+1)));
        );
        header = format(header);
        footer = format('r to the server!');
        set_tab_text(header,footer);
  )
);
```

&nbsp;&nbsp;

### `http_get(url)`

Makes a http get request. Can be used to access APIs or other stuff.

`url` -> String: URL of request

&nbsp;&nbsp;


### `virtual_inventory()`, `virtual_inventory(id)`, `virtual_inventory(id,size)`, `virtual_inventory(id,content)`

Virtual inventories are like enderchest, they exist as a inventory which can be accesses with an id from scarpet.

You can see all available virtual inventories with `virtual_inventories()` which returns a list of all id.

You can create a new inventory with `virtual_inventory(id,size)`. The size defines how many rows the inventory should have (1-6).

Running `virtual_inventory(id)` returns the inventory as a List of items, similar to scarpet's `inventory_get()`.

If you run `virtual_inventory(id,content)` with a list as the `content` argument, you can set the items in the inventory from that list.

To remove an inventory again, run `virtual_inventory(id,null)`

For an example, see examples from `open_inventory()`

### `open_inventory(player,title,inventory)`

Opens a virtual inventory in a GUI for a player.

`player` -> Player: The player tto open the inventory for

`title` -> String: The title of the inventory

`inventory` -> Virtual inventory key

Examples:

`open_inventory(player(),'I got a present for you','present');` Opens the inventory with the id 'present' and displays it to the player

```
virtual_inventory('small',1); //create inventory with one row
inv = virtual_inventory('small'); //read inventory content into variable (will be empty at this point)
inv:4 = ['diamond',64,null]; //place a stack of diamonds in the slot 4 (counting from 0)
virtual_inventory('small',inv); //assign the variable back to the inventory
open_inventory(player('replaceitem','I've got a present for you!','small'); //show the inventory to replaceitem 
```
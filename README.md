# scarpet-additions

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

### `open_inventory(player,title,inventory)`

Opens an inventory GUI for a player.

This has not much use yet, but i may add functions to control it and detect clicks for example.

`player` -> Player: The player tto open the inventory for

`title` -> String: The title of the inventory

`inventory` -> Array of Arrays: `[ [item,count,data] , [item,count,data] , ... ]` an array for the slots, each element is a 3 long array of the item id, count, and the nbt data. This has the same structure as carpet's `inventory_get()` returns.

Examples:

`open_inventory(player(),'Test',inventory_get(player()));` (Copies the players inventory and shows it in a new one. This doesnt sync it, so taking stuff out won't do anything)

`open_inventory(player(),'A little present',[['dead_bush',1,null]]);`

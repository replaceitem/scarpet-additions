# scarpet-additions

![scarpet-additions](src/main/resources/assets/scarpet-additions/icon.png)

## A Carpet extension for some additional scarpet functions

If you need help, message me on discord (replaceitem#9118) or ping me in the [Carpet mod discord](https://discord.gg/gn99m4QRY4)


## Functions

### `convert_color(color,model,output)`

Converts a color from one `model` to another.

`color` -> List: Depending on specified `model`

`model` -> String: Input color model, can be `RGB`, `RGBA` or `HSB`

* RGB: List of a red, green and blue value
* RGBA: List of a red, green, blue and alpha value
* HSB: List of a hue, saturation and brightnes value

`output` -> String: Output color model, can be `RGB`, `RGBA`, `HEX` or `NUM`

* RGB: List of a red, green and blue value
* RGBA: List of a red, green, blue and alpha value
* HEX: String of hex characters (without leading '#') Can be used for `format()`
* NUM: Number representing the color as 4 bytes: 0xRRGGBBAA. Can be used for `'color'` parameter in `draw_shape()`

Examples:

`convert_color([255,128,0],'rgb','hex');` -> `'FF8000'`

`convert_color([255,128,0],'rgb','num');` -> `0xff7f00ff`

`convert_color([0,255,255],'hsb','hex');` -> `'FF0000'`

`convert_color([120,255,255],'hsb','hex');` -> `'00FF00'`

```py
__on_tick() -> (
    if((tick_time() % 2) == 0,
        headerHue = tick_time()%360;
        headerGlossIndex = (floor(tick_time()/3)%40)-10;
        header = [];
        title = 'MinecraftServer';
        for(range(length(title)),
            if(abs(_-headerGlossIndex) < 3,
                c = convert_color([headerHue,abs(_-headerGlossIndex)/3*255,255],'hsb','hex');
            ,
                if(_ < 7,
                    c = convert_color([headerHue,255,190],'hsb','hex');
                ,
                    c = convert_color([headerHue,255,255],'hsb','hex');
                );
            );
            put(header,null,str('b#%s %s',c,slice(title,_,_+1)));
        );
        header = format(header);
        footer = format('r to the server!');
        set_tab_text(header,footer);
  )
);
```

### `http(request_method, url, connect_timeout, read_timeout)`

### `http(request_method, url, connect_timeout, read_timeout, body)`

Makes a http request. Can be used to access APIs or other stuff.

`request_method` -> String: request method, can be GET, POST, HEAD, OPTIONS, PUT, DELETE or TRACE

`url` -> String: URL of request

`connect_timeout` -> number: Time in milliseconds of connection timeout

`read_timeout` -> number: Time in milliseconds of time out for reading data

`body` -> String: body to post when `request_method` is `POST`

Returns a map/list structure converted from the json response.
If the request failed, returns `null`.
If the requested 

Note that this function is blocking,
meaning that if it is executed on the main game thread,
the game will freeze untill the request is done.
To prevent that, use this function inside a `task`.

Example:

```
task(_()->(
    response = http('GET', url, 10000, 10000);
));
```

### `list_text(header, footer, player?)`

Sets header and footer in tab menu of all players, or changes it for one player if `player?` is given.

### `set_motd(motd)`

Sets the message of the day of the server.
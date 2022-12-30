# scarpet-additions

[<img alt="Available for fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@2.8.0/assets/cozy/supported/fabric_vector.svg">](https://fabricmc.net/)
[<img alt="See me on GitHub" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@2.8.0/assets/cozy/social/github-singular_vector.svg">](https://github.com/replaceitem)
[<img alt="Chat on Discord" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@2.8.0/assets/cozy/social/discord-singular_vector.svg">](https://discord.gg/etTDQAVSgt)

![scarpet-additions](https://raw.githubusercontent.com/replaceitem/scarpet-additions/master/src/main/resources/assets/scarpet-additions/icon.png)

## A [Carpet mod](https://modrinth.com/mod/carpet) extension for some additional [scarpet](https://github.com/gnembon/fabric-carpet/wiki/Scarpet) functions

**Reqires [Carpet mod](https://modrinth.com/mod/carpet)**

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

Example:

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

### `http_request(options)`

Performs a http request specified by the given `options`.

This call is blocking, so you should use it in a `task()`!

The `options` parameter is a map value with the following keys:

* `uri` (String): The URI to request from
* `method` (String, optional): The http request method. For example `GET`, `POST`, `DELETE`,... Defaults to `GET`
* `headers` (Map, optional): Each map entry is a string key pointing to a string, or list of strings
* `body` (String): The body for `POST` or other requests

The function returns a map with the following entries:

* `status_code` (number): The status code of the request
* `body` (String): The body returned from the request
* `headers` (Map: string -> [strings]): The received response headers
* `uri` (String): The originally requested URI

Note that the response body is not parsed as json or html escaped.
Use the `escape_html` and `unescape_html` functions,
as well as the scarpet-builtins `encode_json` and `decode_json`.


Example usage:

```js
// simple get request and parsing

response = http_request({
    'uri'->'https://opentdb.com/api.php?amount=1'
});

print('Response: ' + response);

if(response:'status_code' != 200,
    print('Request failed: ' + response:'status_code');
,
    body = decode_json(response:'body');
    print('\n\nBody: ' + body);

    question_data = body:'results':0;
    question = unescape_html(question_data:'question');
    answer = unescape_html(question_data:'correct_answer');

    print('\n\n\n' + question + '\n' + answer);
);
```

### `escape_html(html)`

Returns the escaped html string (e.g. `"` -> `&quot;`)

### `unescape_html(html)`

Returns the unescaped html string (e.g. `&quot;` -> `"`)

### `list_text(header, footer, player?)`

Sets header and footer in tab menu of all players, or changes it for one player if `player?` is given.

### `set_motd(motd)`

Sets the message of the day of the server.

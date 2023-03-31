# Jacks Server Side Tweaks

A collection of gameplay features that are not required on a client.

All features can be configured via the `/jsst` command.

## Features

All features can be enabled or disabled using `/jsst <feature> (enable/disable)`.

### Portable Crafting

Lets you use crafting tables by right-clicking with them. Valid items to check are contained in the
[jsst:items/crafting_tables](src/main/resources/data/jsst/tags/items/crafting_tables.json) tag.

#### Config

| Option    | Description                                                    | Default | Valid Options   |
|-----------|----------------------------------------------------------------|---------|-----------------|
| sneakOnly | Whether players need to sneak to use crafting tables as items. | `false` | `true`, `false` |

### World Container Names

Adds floating labels above containers from either the container name or item count.
![A collection of named containers with their names floating above, along with a diamond sword above a barrel](https://i.imgur.com/PFrsD9q.png)

To give a name to a container, rename it in an anvil. You can use the format `[item:<ITEM_STACK>]` to display an item instead. Examples:
- `[item:golden_apple]`
- `[item:potion{Potion:"night_vision"}]`

To cleanup if you ever remove JSST, you can run the following command to remove lingering entities:

`/kill @e[tag=jsst_world_container_name]`

| Option               | Description                                   | Default  | Valid Options        |
|----------------------|-----------------------------------------------|----------|----------------------|
| facingMode           | How labels should face the player.            | `CENTER` | `CENTER`, `VERTICAL` |
| labelRangeMultiplier | Multiplier for the distance labels are shown. | `1`      | `[0.25, 4]`          |
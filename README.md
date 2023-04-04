# Jacks Server Side Tweaks

A collection of gameplay features that are not required on a client.

All features can be configured via the `/jsst` command.

## Features

All features can be enabled or disabled using `/jsst <feature> (enable/disable)`.

### Portable Crafting

Lets you use crafting tables by right-clicking with them. Valid items to check are contained in the
[jsst:items/crafting_tables](https://github.com/JackFred2/JacksServerSideTweaks/blob/1.19/src/main/resources/data/jsst/tags/items/crafting_tables.json) tag, 
which can be extended using data packs or the Command Defined Datapack feature below. 

#### Config

| Option    | Description                                                    | Default | Valid Options   |
|-----------|----------------------------------------------------------------|---------|-----------------|
| sneakOnly | Whether players need to sneak to use crafting tables as items. | `false` | `true`, `false` |

### World Container Names

Adds floating labels above containers from either the container name or item count. Uses the new Display Entities from 23w06a.

![A collection of named containers with their names floating above, along with a diamond sword above a barrel](https://i.imgur.com/PFrsD9q.png)

To give a name to a container, rename it in an anvil. You can use the format `[item:<ITEM_STACK>]` to display an item instead, using the same syntax as the `/give` command. Examples:
- `[item:minecraft:golden_apple]`
- `[item:potion{Potion:"night_vision"}]`

If you give a container the name `[max-count]`, the item displayed will update to the highest-count item in the container
every time it is opened.

To clean up if you ever remove JSST, you can run the following command to remove lingering display entities:

`/kill @e[tag=jsst_world_container_name]`

#### Config

| Option               | Description                                   | Default  | Valid Options        |
|----------------------|-----------------------------------------------|----------|----------------------|
| facingMode           | How labels should face the player.            | `CENTER` | `CENTER`, `VERTICAL` |
| labelRangeMultiplier | Multiplier for the distance labels are shown. | `1`      | `[0.25, 4]`          |

### Display Items

Lets you make items for display that don't despawn by renaming them to `[display]` in an anvil. Useful for shops or showcases. Plays a chime sound if a display item is successfully made.

**Warning**: This does not prevent the item from being destroyed in other ways, such as cactus, lava, `/kill`, cleanup plugins, or being moved with hoppers or water.

![A collection of colour wool on pedestals that do not despawn.](https://i.imgur.com/8nFBqdu.png)

#### Config

| Option          | Description                                                             | Default | Valid Options   |
|-----------------|-------------------------------------------------------------------------|---------|-----------------|
| ownerPickupOnly | Should only the player who dropped the item be able to pick it back up. | `true`  | `true`, `false` |

### Command Defined Datapack

Lets you create and modify a datapack folder via in-game commands. Currently, only supports tags. Designed to help configure mods
and game features using tags such as `minecraft:enderman_holdable`, or `jsst:crafting_tables`.

`/jsst cdd save` - Manually saves a copy of the current datapack in-memory. Should not be required in most cases as other commands 
that modify the pack call this directly.

#### Tags

See also: [Tag JSON Format](https://minecraft.fandom.com/wiki/Tag#JSON_format)

Base command: `/jsst cdd tag <registry>` 

`<registry>` defines which registry the tags should be looked up through. For example, you'll want `minecraft:item` to modify item tags or `minecraft:block` for block tags.

- `.. listTags [filter]` - Lists all tags that are registered for this registry.
- `.. list <tag>` - Lists all elements in a given tag. This is after tags have been 'flattened' i.e. tags referenced by other tags are not shown.
- `.. add <tag> value <element> [isOptional]` - Adds an element to a `<tag>` in the datapack. 
- `.. add <tag> tag <newTag> [isOptional]` - Adds a reference to `<newTag>` in `<tag>` in the datapack.
- `.. remove <tag> <element>` - Removes an element from `<tag>` in the datapack. This does **not** let you remove items from tags defined elsewhere; use `setReplace` below to overwrite them.
- `.. setReplace <tag> <shouldReplace>` - Marks `<tag>` to overwrite other tags with the same name.

##### Example (Adding nether stars to the crafting table list for Portable Crafting above)

1. `/jsst cdd tag minecraft:item add jsst:crafting_tables value minecraft:nether_star`
2. `/datapack enable "file/jsstCDD"` on first generation
3. `/reload`
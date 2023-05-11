# Jacks Server Side Tweaks

A collection of gameplay features that are not required on a client.

All features can be configured via the `/jsst` command.

## Features

All features can be enabled or disabled using `/jsst <feature> (enable/disable)`.

### Item Editor

By far the largest feature, this provides an easy-to-use, extensive interface for modifying items.

![A GIF showing a 'Legendary Greataxe' being made with enchantments beyond vanilla limits](https://i.imgur.com/BKxEg3C.gif)

Currently supports the following:

- Names, including multi-component texts and custom gradient support
- Lores, with the same features
- Enchantment, with tooltip fix for levels > 10
- Potions (Drinkable, Splash, Lingering, Tipped Arrows, Suspicious Stew)
- [Attribute Modifiers](https://minecraft.fandom.com/wiki/Attribute)
- Container Locks
- Banner/Shields, with support for PlanetMinecraft's [Banner Editor](https://www.planetminecraft.com/banner/).
- Durability Editor, including unbreakable modifiers
- Book Author Editor
- Book Unsigner
- Head Owner Changer
- Leather Armour Colour Editor

and more as ideas come in.

<details>

To begin, run the command `/jsst itemEditor hand` to change the item in your hand, or `.. itemEditor item <item>` to use a template.

![An gif showing various pages of the item editor](https://i.imgur.com/o9W3oM4.gif)

This feature will only show editors possibly usable with the given item.

#### Labels

Enchantments, Potion Effects and Attribute Modifiers have been given custom labels to help see them at a glance; however I can not
provide defaults to every modded enchantment out there. If you want to add your own (either in your mod or a datapack), you can create
a corresponding JSON file under `data/jsst/item_editor_labels/<category>.json`.

In this JSON file, there is a single JSON object with another JSON object under the key `values`. In this values object, there
should be a list of **ID**: **LabelDefinition** pairs; you can find the ID by enabling `enabledDevTools` and browsing said
menu.

**LabelDefinition** is defined as either a minecraft item ID such as `minecraft:diamond_pickaxe`, or a full ItemStack JSON
which you can get from an existing stack by the `enabledDevTools`-only Stack JSON Printer.

You can replace the entire vanilla set by adding `replace: true` alongside the `values` tag at the top level.

For a better explained example, see [JSST's base files](https://github.com/JackFred2/JacksServerSideTweaks/tree/1.19/src/main/resources/data/jsst/item_editor_labels).

#### Config

| Option          | Description                  | Default | Valid Options   |
|-----------------|------------------------------|---------|-----------------|
| enabledDevTools | Enable dev-specific editors. | `true`  | `true`, `false` |

</details>

### Portable Crafting

Lets you use crafting tables by right-clicking with them.

<details>

Valid items to check are contained in the
[jsst:items/crafting_tables](https://github.com/JackFred2/JacksServerSideTweaks/blob/1.19/src/main/resources/data/jsst/tags/items/crafting_tables.json) 
tag, which can be extended using data packs or the Command Defined Datapack feature below.

#### Config

| Option    | Description                                                    | Default | Valid Options   |
|-----------|----------------------------------------------------------------|---------|-----------------|
| sneakOnly | Whether players need to sneak to use crafting tables as items. | `false` | `true`, `false` |

</details>

### World Container Names

![A collection of named containers with their names floating above, along with a diamond sword above a barrel](https://i.imgur.com/PFrsD9q.png)

Adds floating labels above containers from either the container name or item count.

<details>

Uses the new Display Entities from 23w06a.

To give a name to a container, rename it in an anvil. You can use the format `[item:<ITEM_STACK>]` to display an item instead, 
using the same syntax as the `/give` command. Examples:

- `[item:minecraft:golden_apple]`
- `[item:potion{Potion:"night_vision"}]`

If you give a container the name `[max-count]`, the item displayed will update to the highest-count item in the
container
every time it is opened.

#### Admin

To clean up if you ever remove JSST, you can run the following command to remove lingering display entities:

`/kill @e[tag=jsst_world_container_name]`

#### Config

| Option               | Description                                   | Default  | Valid Options        |
|----------------------|-----------------------------------------------|----------|----------------------|
| facingMode           | How labels should face the player.            | `CENTER` | `CENTER`, `VERTICAL` |
| labelRangeMultiplier | Multiplier for the distance labels are shown. | `1`      | `[0.25, 4]`          |

</details>

### Display Items

![A collection of colour wool on pedestals that do not despawn.](https://i.imgur.com/8nFBqdu.png)

Lets you make items for display that don't despawn by renaming them to `[display]` in an anvil.

<details>

Useful for shops or showcases. Plays a chime sound if a display item is successfully made.

**Warning**: This does not prevent the item from being destroyed in other ways, such as cactus, lava, `/kill`, cleanup
plugins, or being moved with hoppers or water.

#### Admin

To 'take ownership' of nearby items, run the following command:

`/execute as @e[type=item,tag=jsst_display_item,distance=..5] run data modify entity @s Owner set from entity @p UUID`

#### Config

| Option          | Description                                                                                                                                              | Default | Valid Options   |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|---------|-----------------|
| ownerPickupOnly | Should only the player who dropped the item be able to pick it back up.                                                                                  | `true`  | `true`, `false` |
| operatorOnly    | Should only server operators be allowed to create display items? Does not apply in single player. Recommended to enable ownerPickupOnly if this is true. | `false` | `true`, `false` |

</details>

### Command Defined Datapack

Lets you create and manage a datapack folder via in-game commands.

<details>

Currently, only supports tags. Designed to help configure mods game features using tags such as `minecraft:enderman_holdable`, 
or `jsst:crafting_tables`, or just creating tags for your server easier.

`/jsst cdd save` - Manually saves a copy of the current datapack in-memory. Should not be required in most cases as other 
commands that modify the pack call this directly.

#### Tags

See also: [Tag JSON Format](https://minecraft.fandom.com/wiki/Tag#JSON_format)

Base command: `/jsst cdd tag <registry>`

`<registry>` defines which registry the tags should be looked up through. For example, you'll want `minecraft:item` to
modify item tags or `minecraft:block` for block tags.

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

</details>

### Banner Writing

Easily write sentences with a stack of blank banners and a command.

![Running a command and writing 'kingdom' effortlessly.](https://i.imgur.com/msoN0cH.gif)

<details>

To start, simply use the command `/jsst bannerWriter start <text>`. This will add a tag to the banners you are
holding, which lets the mod know what to replace.

Uses the letters and numbers from https://www.gamergeeks.net/apps/minecraft/banners/.

Supported letters:

`abcdefghijklmnopqrstuvwxyz0123456789 .,+-*/'"=!:`

</details>

### Sapling Replanter

#### Config

Sapling items try to plant themselves instead of de-spawning. Tries to space them out, and includes support for dark oak saplings.

![Example of replanted saplings, spaced out by a block.](https://i.imgur.com/J6R5cwf.png)

<details>

| Option          | Description                                                                                                     | Default | Valid Options   |
|-----------------|-----------------------------------------------------------------------------------------------------------------|---------|-----------------|
| spacingEnabled  | Should saplings try to space themself out?                                                                      | `true`  | `true`, `false` |
| minimumDistance | Minimum blocks between saplings. Requires `spacingEnabled`. Dark Oak has it's own handling.                     | `1`     | `[1, 3]`        |
| searchRange     | Horizontal distance that saplings search for a valid position. Vertically, always checks layer above and below. | `3`     | `[1, 4]`        |
| maxPerStack     | Maximum number of saplings to plant per dropped stack; the rest are discarded.                                  | `5`     | `[1, 64]`       |

</details>
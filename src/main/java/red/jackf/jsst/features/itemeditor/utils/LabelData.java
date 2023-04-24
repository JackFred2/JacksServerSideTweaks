package red.jackf.jsst.features.itemeditor.utils;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.itemeditor.ItemEditor;
import red.jackf.jsst.features.itemeditor.editors.PotionEditor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Holds the responsibility for loading custom labels from datapacks and other mods - enchantments, attributes, etc. <br />
 * If making your own, create a JSON file for one of the below LabelHolders, under the folder jsst/item_editor_icons/CATEGORY.json.
 * For example: <pre>data/jsst/item_editor_icons/attributes.json</pre>
 * In this file, add a JSON object under the key values, which hold key-pairs of label definitions - either a simple item
 * ID to create a stack of 1, or a full <a href="https://minecraft.fandom.com/wiki/Player.dat_format#Item_structure">Item Stack Structure</a>,
 * without the Slot tag. <br />
 * See this mod's vanilla files for further examples.
 */
public class LabelData {
    private static final String FOLDER = "item_editor_labels";
    public static final LabelHolder<Attribute> ATTRIBUTES = new LabelHolder<>("attributes", a -> new ItemStack(Items.BOOK), Attribute::getDescriptionId);
    public static final LabelHolder<MobEffect> MOB_EFFECTS = new LabelHolder<>("mob_effects", PotionEditor::potionOf, MobEffect::getDescriptionId);
    public static final LabelHolder<Enchantment> ENCHANTMENTS = new LabelHolder<>("enchantments", e -> {
        var stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(Map.of(e, e.getMaxLevel()), stack);
        return stack;
    }, Enchantment::getDescriptionId);
    public static void setup() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return JSST.id(FOLDER);
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                ATTRIBUTES.reload(manager);
                MOB_EFFECTS.reload(manager);
                ENCHANTMENTS.reload(manager);
            }
        });
    }

    public static class LabelHolder<T> {
        private static final String REPLACE = "replace";
        private static final String VALUES = "values";
        private static final Gson GSON = new GsonBuilder().create();
        private final Function<T, ItemStack> defaultGetter;
        private final Function<T, String> keyGetter;
        private final HashMap<String, ItemStack> icons = new HashMap<>();
        private final String category;

        private LabelHolder(String category,  Function<T, ItemStack> defaultGetter, Function<T, String> keyGetter) {
            this.category = category;
            this.defaultGetter = defaultGetter;
            this.keyGetter = keyGetter;
        }

        private void reload(ResourceManager manager) {
            this.icons.clear();

            for (var entry : manager.listResourceStacks(FOLDER, path -> path.getPath().equals("%s/%s.json".formatted(FOLDER, category))).entrySet()) {
                if (!entry.getKey().getNamespace().equals(JSST.ID)) continue;
                for (var resource : entry.getValue()) {
                    try (var reader = new InputStreamReader(resource.open())) {
                        var json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                        parseJson(json);
                    } catch (JsonParseException | IOException ex) {
                        ItemEditor.LOGGER.error("Error reading icon file %s".formatted(entry.getKey()), ex);
                    }
                }
            }
        }

        public ItemStack get(T key) {
            return icons.getOrDefault(keyGetter.apply(key), defaultGetter.apply(key));
        }

        public Map<T, ItemStack> getIconList(Registry<T> registry) {
            return registry.stream()
                    .sorted(Comparator.comparing(keyGetter))
                    .map(t -> Pair.of(t, Labels.create(get(t))
                            .withName(Component.translatable(keyGetter.apply(t)).withStyle(CommandUtils.CLEAN))
                            .withDebugHint(keyGetter.apply(t))
                            .build()))
                    .collect(EditorUtils.pairLinkedMapCollector());
        }

        private void parseJson(JsonObject file) {
            if (file.has(REPLACE) && file.get(REPLACE) instanceof JsonPrimitive prim && prim.isBoolean() && prim.getAsBoolean()) {
                icons.clear();
            }
            if (file.has(VALUES) && file.get(VALUES) instanceof JsonObject object) {
                for (var key : object.keySet()) {
                    var stack = parseLabel(object.get(key));
                    if (stack != null) {
                        //ItemEditor.LOGGER.info("%s: %s -> %s".formatted(category, key, stack));
                        icons.put(key, stack);
                    }
                }
            }
        }

        @Nullable
        private static ItemStack parseLabel(JsonElement json) throws JsonParseException {
            if (json instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isString()) {
                var resloc = new ResourceLocation(jsonPrimitive.getAsString());
                if (BuiltInRegistries.ITEM.containsKey(resloc)) {
                    return new ItemStack(BuiltInRegistries.ITEM.get(resloc));
                } else {
                    throw new JsonParseException("Unknown item: %s".formatted(resloc));
                }
            } else if (json instanceof JsonObject) {
                var decoded = ItemStack.CODEC.decode(JsonOps.INSTANCE, json);
                return decoded.resultOrPartial(s -> {
                    throw new JsonParseException(s);
                }).map(Pair::getFirst).orElse(null);
            } else {
                throw new JsonParseException("Not an item ID or stack: %s".formatted(json));
            }
        }
    }
}

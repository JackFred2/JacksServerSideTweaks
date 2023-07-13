package red.jackf.jsst.features.itemeditor.editors;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("resource") // dont close the world
public class ArmourTrimEditor extends Editor {
    private final RegistryAccess registries;
    @Nullable
    private ArmorTrim trim;
    public ArmourTrimEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        this.registries = player.serverLevel().registryAccess();
        this.trim = ArmorTrim.getTrim(registries, stack).orElse(null);
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.NETHERITE_CHESTPLATE)
                .withName("Edit Trim")
                .build();
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(ItemTags.TRIMMABLE_ARMOR);
    }

    private ItemStack build() {
        var copy = getOriginal();
        if (trim == null) {
            copy.removeTagKey(ArmorTrim.TAG_TRIM_ID);
        } else {
            ArmorTrim.setTrim(registries, copy, trim);
        }

        return copy;
    }

    @Override
    public void open() {
        var materials = registries.registryOrThrow(Registries.TRIM_MATERIAL);
        var patterns = registries.registryOrThrow(Registries.TRIM_PATTERN);

        var elements = new HashMap<Integer, ItemGuiElement>();

        elements.put(0, new ItemGuiElement(Labels.create(build()).withHint("Click to finish").build(), () -> {
            stack = build();
            complete();
        }));

        elements.put(1, EditorUtils.divider());

        if (trim != null) {
            elements.put(2, new ItemGuiElement(Labels.create(trim.material().value().ingredient().value()).withHint("Click to change material")
                    .build(), () -> {
                var options = materials.stream().map(mat -> {
                            var stack = new ItemStack(mat.ingredient().value());
                            return Pair.of(mat, stack);
                        }).collect(EditorUtils.pairLinkedMapCollector());
                Sounds.interact(player);
                Menus.selector(player, options, CancellableCallback.of(mat -> {
                    Sounds.success(player);
                    this.trim = new ArmorTrim(Holder.direct(mat), trim.pattern());
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));

            elements.put(3, new ItemGuiElement(Labels.create(trim.pattern().value().templateItem().value()).withHint("Click to change pattern")
                    .build(), () -> {
                Map<TrimPattern, ItemStack> options = patterns.stream()
                        .map(mat -> Pair.of(mat, new ItemStack(mat.templateItem().value())))
                        .collect(EditorUtils.pairLinkedMapCollector());
                Sounds.interact(player);
                Menus.selector(player, options, CancellableCallback.of(pattern -> {
                    Sounds.success(player);
                    this.trim = new ArmorTrim(trim.material(), Holder.direct(pattern));
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));

            elements.put(4, new ItemGuiElement(Labels.create(Items.GRINDSTONE).withHint("Clear").build(), () -> {
                Sounds.grind(player);
                this.trim = null;
                open();
            }));
        } else {
            elements.put(2, new ItemGuiElement(Labels.create(Items.NETHER_STAR).withName("Add Pattern").build(), () -> {
                Sounds.success(player);
                var material = materials.getRandom(player.getRandom())
                        .orElse(materials.getHolderOrThrow(TrimMaterials.GOLD));
                var pattern = patterns.getRandom(player.getRandom())
                        .orElse(patterns.getHolderOrThrow(TrimPatterns.SENTRY));
                this.trim = new ArmorTrim(material, pattern);
                open();
            }));
        }

        player.openMenu(EditorUtils.make9x1(Component.literal("Editing Armour Trim"), elements));
    }
}

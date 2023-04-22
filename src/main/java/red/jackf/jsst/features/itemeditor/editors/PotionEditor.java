package red.jackf.jsst.features.itemeditor.editors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PotionEditor extends Editor {
    private static final int MAX_EFFECTS = 12;
    private static final Map<Item, Integer> INVERSE_DURATION_MULTIPLIERS = Map.of(
            Items.LINGERING_POTION, 4,
            Items.TIPPED_ARROW, 8);

    private Potion base;
    private List<MobEffectInstance> custom = new ArrayList<>();
    @Nullable
    private Colour customColour = null;

    private DurationModifier mitigation = DurationModifier.IGNORE;

    private int page;
    public PotionEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        readStack();
    }

    public static ItemStack potionOf(MobEffect effect) {
        var stack = new ItemStack(Items.POTION);
        PotionUtils.setPotion(stack, Potions.WATER);
        PotionUtils.setCustomEffects(stack, List.of(new MobEffectInstance(effect, 1, 0)));
        return stack;
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(Items.TIPPED_ARROW) || stack.getItem() instanceof PotionItem;
    }

    private void readStack() {
        this.base = PotionUtils.getPotion(stack);
        this.custom = PotionUtils.getCustomEffects(stack);
        var tag = stack.getTag();
        if (tag != null && tag.contains(PotionUtils.TAG_CUSTOM_POTION_COLOR, Tag.TAG_INT))
            customColour = new Colour(tag.getInt(PotionUtils.TAG_CUSTOM_POTION_COLOR));
        else
            customColour = null;
    }

    private void reset() {
        Sounds.clear(player);
        this.stack = getOriginal();
        readStack();
        open();
    }

    @Override
    public ItemStack label() {
        return PotionUtils.setPotion(Labels.create(Items.POTION).withName("Edit Potion").build(), Potions.HEALING);
    }

    private void build() {
        PotionUtils.setPotion(stack, base);
        if (custom.isEmpty()) {
            stack.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_EFFECTS);
        } else {
            if (this.mitigation == DurationModifier.MITIGATE) {
                PotionUtils.setCustomEffects(stack, custom.stream().map(effect -> {
                    if (effect.getEffect().isInstantenous() || effect.isInfiniteDuration()) return effect;
                    var duration = effect.getDuration();
                    duration *= INVERSE_DURATION_MULTIPLIERS.getOrDefault(stack.getItem(), 1);
                    duration /= INVERSE_DURATION_MULTIPLIERS.getOrDefault(getOriginal().getItem(), 1);
                    return new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier());
                }).toList());
            } else {
                PotionUtils.setCustomEffects(stack, custom);
            }
        }
        var tag = stack.getTag();
        if (customColour != null)
            stack.getOrCreateTag().putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, customColour.value());
        else if (tag != null)
            tag.remove(PotionUtils.TAG_CUSTOM_POTION_COLOR);
    }

    @Override
    public void open() {
        build();
        var elements = new HashMap<Integer, ItemGuiElement>();

        for (int i = 3; i < 54; i += 9) elements.put(i, EditorUtils.divider());
        for (int i = 13; i < 18; i++) elements.put(i, EditorUtils.divider());

        elements.put(10, new ItemGuiElement(Labels.create(stack).keepLore().withHint("Click to finish").build(), this::complete));

        elements.put(27, Selector.create(PotionType.class, "Set Potion Type", PotionType.BY_ITEM.get(stack.getItem()), type -> {
            Sounds.interact(player);
            var newStack = new ItemStack(PotionType.BY_ITEM.inverse().get(type));
            newStack.setTag(stack.getTag());
            stack = newStack;
            open();
        }));
        elements.put(28, Selector.create(DurationModifier.class, "Duration Mitigation", mitigation, newValue -> {
            Sounds.interact(player);
            this.mitigation = newValue;
            open();
        }));

        elements.put(36, new ItemGuiElement(Labels.create(Items.WATER_BUCKET).withName("Clear").build(), () -> {
            Sounds.clear(player);
            this.base = Potions.WATER;
            this.custom.clear();
            this.customColour = null;
            open();
        }));
        elements.put(37, new ItemGuiElement(Labels.create(Items.LAVA_BUCKET).withName("Clear Custom Only").build(), () -> {
            Sounds.clear(player);
            this.custom.clear();
            this.customColour = null;
            open();
        }));
        if (customColour != null) elements.put(38, new ItemGuiElement(Labels.create(Items.GUNPOWDER).withName("Clear Custom Colour").build(), () -> {
            Sounds.clear(player);
            this.customColour = null;
            open();
        }));
        elements.put(45, new ItemGuiElement(Labels.create(Items.GLOWSTONE_DUST).withName("Custom Colour").build(), () -> {
            Sounds.interact(player);
            Menus.colour(player, CancellableCallback.of(newColour -> {
                Sounds.success(player);
                this.customColour = newColour;
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        elements.put(46, EditorUtils.reset(this::reset));
        elements.put(47, EditorUtils.cancel(this::cancel));

        var basePreview = PotionUtils.setPotion(new ItemStack(Items.POTION), base);
        elements.put(4, new ItemGuiElement(Labels.create(basePreview).withHint("Click to change base potion").keepLore().build(), () -> {
            Sounds.interact(player); // open potion selector
            var options = BuiltInRegistries.POTION.stream().collect(Collectors.toMap(p -> p, p -> PotionUtils.setPotion(new ItemStack(Items.POTION), p), (p1, p2) -> p2, LinkedHashMap::new));
            Menus.selector(player, options, CancellableCallback.of(p -> {
                Sounds.success(player);
                this.base = p;
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));

        var maxPage = (custom.size() / 3) - (custom.size() >= MAX_EFFECTS ? 1 : 0);
        this.page = Mth.clamp(this.page, 0, maxPage);
        EditorUtils.drawPage(elements, custom, page, maxPage, newPage -> {
            Sounds.interact(player, 1f + ((float) (newPage + 1) / (maxPage + 1)) / 2);
            this.page = newPage;
            open();
        }, 4, (slot, index) -> {
            var effect = custom.get(index);

            // Main preview with effect selector
            elements.put(slot, new ItemGuiElement(Labels.create(potionOf(effect.getEffect()))
                    .withName(effect.getEffect().getDisplayName().copy().withStyle(Labels.CLEAN))
                    .withHint(effect.getEffect().isInstantenous() ? "Instant" : EditorUtils.formatDuration(effect.getDuration()))
                    .build(), () -> {
                Sounds.interact(player);
                Menus.mobEffect(player, CancellableCallback.of(newEffect -> {
                    Sounds.success(player);
                    custom.set(index, new MobEffectInstance(newEffect, effect.getDuration(), effect.getAmplifier()));
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));
            var offset = 1;
            if (!effect.getEffect().isInstantenous()) {

                // Duration Selector
                elements.put(slot + offset++, new ItemGuiElement(Labels.create(Items.CLOCK).withName("Set Duration").build(), () -> {
                    Sounds.interact(player);
                    Menus.duration(player, effect.getDuration(), CancellableCallback.of(newDuration -> {
                        if (newDuration == 0) {
                            Sounds.error(player);
                            custom.remove((int) index);
                        } else {
                            Sounds.success(player);
                            custom.set(index, new MobEffectInstance(effect.getEffect(), newDuration, effect.getAmplifier()));
                        }
                        open();
                    }, () -> {
                        Sounds.error(player);
                        open();
                    }));
                }));

                // Infinite Duration Button
                if (effect.getDuration() != -1)
                    elements.put(slot + offset++, new ItemGuiElement(Labels.create(Items.HEART_OF_THE_SEA).withName("Make Infinite").build(), () -> {
                        Sounds.success(player);
                        custom.set(index, new MobEffectInstance(effect.getEffect(), -1, effect.getAmplifier()));
                        open();
                    }));
            }

            // Amplifier Selector
            elements.put(slot + offset, new ItemGuiElement(Labels.create(Items.ANVIL).withName("Set Amplifier").build(), () -> {
                Sounds.interact(player);
                Menus.string(player, String.valueOf(effect.getAmplifier() + 1), CancellableCallback.of(s -> {
                    try {
                        int parsed = Integer.parseUnsignedInt(s);
                        if (parsed == 0) {
                            Sounds.error(player);
                            custom.remove((int) index);
                        } else {
                            Sounds.success(player);
                            custom.set(index, new MobEffectInstance(effect.getEffect(), effect.getDuration(), Mth.clamp(parsed - 1, 0, 127)));
                        }
                        open();
                    } catch (NumberFormatException ex) {
                        Sounds.error(player);
                        open();
                    }
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));
        }, index -> {
            Sounds.error(player);
            custom.remove((int) index);
            open();
        }, () -> {
            Sounds.interact(player);
            custom.add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300 * SharedConstants.TICKS_PER_SECOND, 0));
            open();
        });

        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Potion Effects"), elements));
    }

    public enum PotionType implements Selector.Labeled {
        DRINKABLE("Drinkable", Labels.create(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING)).build()),
        SPLASH("Splash", Labels.create(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.HEALING)).build()),
        LINGERING("Lingering", Labels.create(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.HEALING)).build()),
        TIPPED_ARROW("Tipped Arrow", Labels.create(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.HEALING)).build());

        private static final BiMap<Item, PotionType> BY_ITEM = HashBiMap.create(Map.of(
                Items.POTION, DRINKABLE,
                Items.SPLASH_POTION, SPLASH,
                Items.LINGERING_POTION, LINGERING,
                Items.TIPPED_ARROW, TIPPED_ARROW
        ));

        private final String settingName;
        private final ItemStack label;

        PotionType(String settingName, ItemStack label) {
            this.settingName = settingName;
            this.label = label;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }

    public enum DurationModifier implements Selector.Labeled {
        IGNORE("None", Labels.create(Items.LEVER).build()),
        MITIGATE("Mitigate Durations Multipliers", Labels.create(Items.REDSTONE_TORCH).build());

        private final String settingName;
        private final ItemStack label;

        DurationModifier(String settingName, ItemStack label) {
            this.settingName = settingName;
            this.label = label;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }
}

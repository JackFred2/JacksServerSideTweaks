package red.jackf.jsst.features.itemeditor.editors;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.menus.MobEffectMenu;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SuspiciousStewEditor extends Editor {
    private static final Integer MAX_EFFECTS = 10;
    private static final Map<Item, SuspiciousEffectHolder> EFFECT_ITEMS = BuiltInRegistries.ITEM.stream()
            .map(item -> Pair.of(item, SuspiciousEffectHolder.tryGet(item)))
            .filter(pair -> pair.right() != null)
            .collect(EditorUtils.linkedMapCollector(Pair::left, Pair::right));
    private static final Map<SuspiciousEffectHolder, ItemStack> PRESETS = EFFECT_ITEMS.entrySet().stream()
            .map(entry -> {
                var hint = entry.getValue().getSuspiciousEffect().getDisplayName().copy().setStyle(Labels.HINT)
                        .append(Component.literal(": " + EditorUtils.formatDuration(entry.getValue().getEffectDuration())).withStyle(Labels.HINT));
                return Pair.of(entry.getValue(), Labels.create(entry.getKey()).withHint(hint).build());
            })
            .collect(EditorUtils.linkedMapCollector(Pair::left, Pair::right));

    private final List<MobEffectInstance> effects = new ArrayList<>();

    private int page = 0;

    public SuspiciousStewEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        loadFromStack();
    }

    private void loadFromStack() {
        effects.clear();
        SuspiciousStewItem.listPotionEffects(stack, effects::add);
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(Items.SUSPICIOUS_STEW);
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.SUSPICIOUS_STEW).withName("Edit Suspicious Stew").build();
    }

    private ItemStack build() {
        var copy = stack.copy();
        copy.removeTagKey(SuspiciousStewItem.EFFECTS_TAG);
        effects.forEach(effect -> SuspiciousStewItem.saveMobEffect(copy, effect.getEffect(), effect.getDuration()));
        return copy;
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(Labels.create(build()).keepLore().withHint("Click to finish")
                .build(), () -> {
            this.stack = build();
            complete();
        }));

        for (int i = 3; i < 54; i += 9)
            elements.put(i, EditorUtils.divider());

        elements.put(45, EditorUtils.clear(() -> {
            Sounds.clear(player);
            effects.clear();
            open();
        }));
        elements.put(46, EditorUtils.reset(() -> {
            Sounds.clear(player);
            this.stack = getOriginal();
            loadFromStack();
            open();
        }));
        elements.put(47, EditorUtils.cancel(this::cancel));

        elements.put(36, new ItemGuiElement(Labels.create(Items.BLUE_ORCHID).withName("Choose Vanilla Preset").build(), () -> {
            Sounds.interact(player);
            Menus.selector(player, PRESETS, CancellableCallback.of(holder -> {
                Sounds.success(player);
                this.effects.clear();
                this.effects.add(new MobEffectInstance(holder.getSuspiciousEffect(), holder.getEffectDuration()));
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));

        var maxPage = (effects.size() / 5) - (effects.size() >= MAX_EFFECTS ? 1 : 0);
        this.page = Mth.clamp(this.page, 0, maxPage);
        EditorUtils.drawPage(elements, effects, page, maxPage, newPage -> {
            Sounds.interact(player, 1f + ((float) (page + 1) / (maxPage + 1)) / 2);
            this.page = newPage;
            open();
        }, 6, (slot, index) -> {
            var effect = effects.get(index);

            // main effect preview
            elements.put(slot, new ItemGuiElement(Labels.create(MobEffectMenu.potionOf(effect.getEffect()))
                    .withName(effect.getEffect().getDisplayName().copy().withStyle(Labels.CLEAN))
                    .withHint(EditorUtils.formatDuration(effect.getDuration()))
                    .withHint("Click to change effect")
                    .build(), () -> Menus.mobEffect(player, CancellableCallback.of(newEffect -> {
                        Sounds.success(player);
                        effects.set(index, new MobEffectInstance(newEffect, effect.getDuration()));
                        open();
                    }, () -> {
                        Sounds.error(player);
                        open();
                    }))));

            elements.put(slot + 1, new ItemGuiElement(Labels.create(Items.CLOCK).withName("Set Duration").build(), () -> {
                Sounds.interact(player);
                Menus.duration(player, effect.getDuration(), CancellableCallback.of(newDuration -> {
                    if (newDuration == 0) {
                        Sounds.error(player);
                        effects.remove((int) index);
                    } else {
                        Sounds.success(player);
                        effects.set(index, new MobEffectInstance(effect.getEffect(), newDuration));
                    }
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));
        }, index -> {
            Sounds.error(player);
            effects.remove((int) index);
            open();
        }, () -> {
            Sounds.success(player);
            effects.add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 8 * SharedConstants.TICKS_PER_SECOND));
            open();
        });

        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Suspicious Stew"), elements));
    }
}

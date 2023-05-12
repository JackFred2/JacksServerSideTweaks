package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

public class FireworkEditor extends Editor {
    private static final int MAX_STARS = 15;

    private int page = 0;

    public FireworkEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.FIREWORK_ROCKET).withName("Edit Firework").build();
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(Items.FIREWORK_ROCKET);
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        var tag = stack.getOrCreateTag();
        CompoundTag fireworksTag;
        if (tag.contains(FireworkRocketItem.TAG_FIREWORKS, Tag.TAG_COMPOUND)) {
            fireworksTag = tag.getCompound(FireworkRocketItem.TAG_FIREWORKS);
        } else {
            fireworksTag = new CompoundTag();
            tag.put(FireworkRocketItem.TAG_FIREWORKS, fireworksTag);
        }

        elements.put(10, new ItemGuiElement(Labels.create(stack).keepLore().withHint("Click to finish").build(), this::complete));

        int duration = Mth.clamp(fireworksTag.getByte(FireworkRocketItem.TAG_FLIGHT), 1, 127);

        elements.put(38, new ItemGuiElement(Labels.create(Items.CLOCK)
                .withName("Change Flight Time")
                .withHint(duration > 0 ? String.valueOf(duration) : "Unknown Time")
                .build(), () -> {
            Sounds.interact(player);
            Menus.integer(player, duration, CancellableCallback.of(i -> {
                Sounds.success(player);
                fireworksTag.putByte(FireworkRocketItem.TAG_FLIGHT, (byte) Mth.clamp(i, 1, 127));
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));

        elements.put(45, EditorUtils.clear(() -> {
            Sounds.clear(player);
            fireworksTag.remove(FireworkRocketItem.TAG_EXPLOSIONS);
            open();
        }));

        elements.put(46, EditorUtils.reset(() -> {
            Sounds.clear(player);
            this.stack = getOriginal();
            open();
        }));

        elements.put(47, EditorUtils.cancel(this::cancel));

        for (int i = 3; i < 54; i += 9) elements.put(i, EditorUtils.divider());

        var stars = fireworksTag.getList(FireworkRocketItem.TAG_EXPLOSIONS, Tag.TAG_COMPOUND);

        var maxPage = (stars.size() / 5) - (stars.size() >= MAX_STARS ? 1 : 0);
        this.page = Mth.clamp(this.page, 0, maxPage);

        EditorUtils.drawPage(elements, stars, page, maxPage, newPage -> {
            Sounds.interact(player, 1f + ((float) (newPage + 1) / (maxPage + 1)) / 2);
            this.page = newPage;
            open();
        }, 6, (slot, index) -> {
            var explosion = stars.get(index);
            var fakeStack = new ItemStack(Items.FIREWORK_STAR);
            fakeStack.getOrCreateTag().put(FireworkRocketItem.TAG_EXPLOSION, explosion);
            elements.put(slot, new ItemGuiElement(Labels.create(fakeStack).withHint("Click to edit").build(), () -> {
                Sounds.interact(player);
                new FireworkStarEditor(fakeStack, player, returned -> {
                    if (returned != fakeStack) {
                        stars.set(index, returned.getTagElement(FireworkRocketItem.TAG_EXPLOSION));
                    }
                    open();
                }).open();
            }));
        }, index -> {
            Sounds.error(player);
            stars.remove((int) index);
            open();
        }, () -> {
            Sounds.interact(player);
            var newTag = new CompoundTag();
            newTag.putInt(FireworkRocketItem.TAG_EXPLOSION_TYPE, new Random().nextInt(FireworkRocketItem.Shape.values().length));
            newTag.putIntArray(FireworkRocketItem.TAG_EXPLOSION_COLORS, new int[]{Colour.fromHsv(new Random().nextFloat(), 1, 1).value()});
            stars.add(newTag);
            if (!fireworksTag.contains(FireworkRocketItem.TAG_EXPLOSIONS, Tag.TAG_LIST)) fireworksTag.put(FireworkRocketItem.TAG_EXPLOSIONS, stars);
            open();
        });

        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Firework"), elements));
    }
}

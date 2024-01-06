package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.util.sgui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DurabilityEditor extends GuiEditor {
    public static final EditorType TYPE = new EditorType(
            DurabilityEditor::new,
            false,
            stack -> stack.getItem().canBeDepleted(),
            DurabilityEditor::getLabel
    );

    public DurabilityEditor(
            ServerPlayer player,
            EditorContext context,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x4, player, context, initial, callback);
        this.setTitle(Component.translatable("jsst.itemEditor.durability"));

        this.setSlot(Util.slot(0, 3), CommonLabels.cancel(this::cancel));
    }

    public static GuiElementBuilder getLabel() {
        return GuiElementBuilder.from(new ItemStack(Items.CRACKED_STONE_BRICKS))
                                .setName(Component.translatable("jsst.itemEditor.durability"));
    }

    @Override
    protected void redraw() {
        this.drawPreview(Util.slot(1, 1));

        for (int row = 0; row < 4; row++) this.setSlot(Util.slot(3, row), CommonLabels.divider());

        var options = new ArrayList<GuiElementInterface>();
        int maxDamage = stack.getMaxDamage();

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean("Unbreakable")) {
            // durability percent
            for (var entry : List.of(1.0, 0.99, 0.75, 0.5, 0.25, 0.1, 0.025, 0.01)) {
                final int damage = (int) (maxDamage * (1 - entry));
                var preview = stack.copy();
                preview.setDamageValue(damage);
                options.add(GuiElementBuilder.from(preview)
                                             .setName(Component.translatable("optimizeWorld.progress.percentage", "%.0f".formatted(entry * 100)))
                                             .addLoreLine(Hints.leftClick(Translations.select()))
                                             .setCallback(Inputs.leftClick(() -> {
                                                 Sounds.click(player);
                                                 this.stack.setDamageValue(damage);
                                                 this.redraw();
                                             })).build());
            }

            // durability numbers
            for (var entry : List.of(10000, 5000, 2500, 1000, 500, 250, 100, 50, 25, 10, 5, 1)) {
                if (entry >= maxDamage) continue;
                final int damage = maxDamage - entry;
                var preview = stack.copy();
                preview.setDamageValue(damage);
                options.add(GuiElementBuilder.from(preview)
                                             .setName(Component.literal(String.valueOf(entry)))
                                             .addLoreLine(Hints.leftClick(Translations.select()))
                                             .setCallback(Inputs.leftClick(() -> {
                                                 Sounds.click(player);
                                                 this.stack.setDamageValue(damage);
                                                 this.redraw();
                                             })).build());
            }

            var preview = stack.copy();
            preview.setDamageValue(0);
            options.add(GuiElementBuilder.from(preview)
                                         .glow()
                                         .setName(Component.translatable("item.unbreakable").withStyle(Styles.POSITIVE))
                                         .addLoreLine(Hints.leftClick(Translations.select()))
                                         .setCallback(Inputs.leftClick(() -> {
                                             Sounds.click(player);
                                             stack.getOrCreateTag().putBoolean("Unbreakable", true);
                                             this.redraw();
                                         })).build());
        } else {
            options.add(GuiElementBuilder.from(stack)
                                         .glow()
                                         .setName(Component.translatable("item.unbreakable").withStyle(Styles.NEGATIVE.withStrikethrough(true)))
                                         .addLoreLine(Hints.leftClick(Translations.select()))
                                         .setCallback(Inputs.leftClick(() -> {
                                             Sounds.click(player);
                                             stack.removeTagKey("Unbreakable");
                                             this.redraw();
                                         })).build());
        }

        var optionSlots = Util.slotTranslator(4, 9, 0, 4);
        optionSlots.fill(this, ItemStack.EMPTY);

        for (var entry : optionSlots.iterate(options)) {
            this.setSlot(entry.slot(), entry.item());
        }
    }
}

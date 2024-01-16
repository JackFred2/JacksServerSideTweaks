package red.jackf.jsst.feature.itemeditor.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.elements.ToggleButton;
import red.jackf.jsst.util.sgui.elements.WrappedElement;

import java.util.ArrayList;
import java.util.List;

public class ItemEditorGui extends SimpleGui {
    private final EquipmentSlot returnSlot;
    private final EditorContext context;
    private ItemStack stack;
    private boolean developerMode;

    public ItemEditorGui(
            ServerPlayer player,
            ItemStack initialStack,
            @Nullable EquipmentSlot returnSlot,
            EditorContext context) {
        super(MenuType.GENERIC_9x5, player, false);
        this.stack = initialStack.copy();
        this.returnSlot = returnSlot;
        this.context = context;

        this.developerMode = !context.cosmeticOnly() && FabricLoader.getInstance().isDevelopmentEnvironment();

        this.setTitle(Component.translatable("jsst.itemEditor.title"));

        this.drawStatic();
    }

    private void drawStatic() {
        for (int row = 0; row < 5; row++) this.setSlot(row * 9 + 3, CommonLabels.divider());

        this.setSlot(Util.slot(0, 4), CommonLabels.cancel(this::close));
    }

    private void redraw() {
        if (!this.context.cosmeticOnly())
            this.setSlot(Util.slot(0, 3), ToggleButton.builder()
                    .initial(this.developerMode)
                    .disabled(Items.COAL.getDefaultInstance())
                    .enabled(Items.DIAMOND.getDefaultInstance())
                    .label(Component.translatable("jsst.itemEditor.title.developerMode"))
                    .setCallback(mode -> {
                        Sounds.click(player);
                        this.developerMode = mode;
                        this.redraw();
                    }).build());

        final List<WrappedElement> editors = this.context.allowedEditors().stream()
                .filter(type -> !type.developer() || this.developerMode)
                .filter(type -> type.appliesTo().test(stack))
                .map(editorType -> {
                    List<Component> lore = new ArrayList<>();
                    lore.add(Hints.leftClick(Translations.open()));
                    if (this.developerMode)
                        lore.add(Component.literal("ID: " + editorType.id()).withStyle(Styles.ID));

                    return new WrappedElement(
                            editorType.labelSupplier().apply(context).build(),
                            lore,
                            Inputs.leftClick(() -> editorType.constructor()
                                    .create(this.player, this.context, this.stack, this::onResult)
                                    .run()));
                }).toList();

        // update editors
        final GridTranslator gridTranslator = GridTranslator.between(4, 9, 0, 5);
        gridTranslator.fill(this, ItemStack.EMPTY);

        for (var pair : gridTranslator.iterate(editors)) {
            this.setSlot(pair.slot(), pair.item());
        }
    }

    @Override
    public void onOpen() {
        // update result stack
        this.setSlot(Util.slot(1, 1), JSSTElementBuilder.from(stack)
                .setName(Util.getLabelAsTooltip(stack))
                .leftClick(Translations.save(), this::complete));

        this.redraw();
    }

    private void onResult(ItemStack result) {
        this.stack = result;
        this.open();
    }

    private void complete() {
        Sounds.click(player);
        if (returnSlot != null) {
            this.player.setItemSlot(returnSlot, this.stack);
        } else {
            this.player.getInventory().placeItemBackInInventory(this.stack);
        }
        this.close();
    }
}

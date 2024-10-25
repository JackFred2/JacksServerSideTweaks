package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonLabels;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.UIRegion;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.GridPaginator;

import java.util.function.Consumer;

public class ArmourTrimEditor extends GuiEditor {
    public static final Type<ArmourTrimEditor> TYPE = new Type<>(
            JSST.id("armour_trim"),
            ArmourTrimEditor::new,
            session -> session.getStack().is(ItemTags.TRIMMABLE_ARMOR),
            session -> JSSTElementBuilder.from(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE.getDefaultInstance())
                    .setName(Component.translatable("jsst.itemEditor.editor.armourTrim"))
                    .hideDefaultTooltip()
                    .build()
    );

    private final GridPaginator<TrimMaterial> materialPages = GridPaginator.<TrimMaterial>builder(this)
            .slots(UIRegion.playerRectangle(this, 2, 0, 5, 3))
            .fullButtons(this.getPlayerSlotFor(2, 3), this.getPlayerSlotFor(3, 3), this.getPlayerSlotFor(4, 3))
            .elements(this.session.getPlayer().registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).stream().toList())
            .drawFunction(material -> JSSTElementBuilder.from(material.ingredient().value())
                    .setName(material.description())
                    .leftClick(Translations.select(), () -> {
                        Sounds.UI.click(player);
                        this.setMaterial(material);
                        this.refresh();
                    }).build())
            .build();

    private final GridPaginator<TrimPattern> patternPages = GridPaginator.<TrimPattern>builder(this)
            .slots(UIRegion.playerRectangle(this, 6, 0, 9, 3))
            .fullButtons(this.getPlayerSlotFor(6, 3), this.getPlayerSlotFor(7, 3), this.getPlayerSlotFor(8, 3))
            .elements(this.session.getPlayer().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).stream().toList())
            .drawFunction(pattern -> JSSTElementBuilder.from(pattern.templateItem().value())
                    .setName(pattern.description())
                    .hideDefaultTooltip()
                    .leftClick(Translations.select(), () -> {
                        Sounds.UI.click(player);
                        this.setPattern(pattern);
                        this.refresh();
                    }).build())
            .build();

    public ArmourTrimEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.armourTrim"), MenuType.SMITHING, true);
    }

    @Override
    protected void drawStatic() {
        UIRegion.playerColumn(this, 1).fillStack(CommonLabels::divider);
        UIRegion.playerColumn(this, 5).fillStack(CommonLabels::divider);

        this.setPlayerSlot(0, 3, CommonLabels.cancel(this::cancel));
    }

    @Override
    protected void refresh() {
        materialPages.draw();
        patternPages.draw();

        if (this.stack.has(DataComponents.TRIM)) {
            this.setPlayerSlot(0, 2, JSSTElementBuilder.from(Items.GRINDSTONE).ui()
                    .leftClick(Translations.clear(), () -> {
                        Sounds.UI.grind(player);
                        this.stack.remove(DataComponents.TRIM);
                        this.refresh();
                    }));
        } else {
            this.clearPlayerSlot(0, 2);
        }
    }

    private void setMaterial(TrimMaterial material) {
        
    }

    private void setPattern(TrimPattern pattern) {

    }
}

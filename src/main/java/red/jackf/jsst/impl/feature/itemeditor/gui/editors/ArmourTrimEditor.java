package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
//? if <=1.21.1 {
/*import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
*///?} else {
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
//?}
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonLabels;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.GridPaginator;

import java.util.Optional;
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

    private final GridPaginator<TrimPattern> patternPages = GridPaginator.<TrimPattern>builder(this)
            .slots(UIRegion.playerRectangle(this, 0, 0, 3, 3))
            .fullButtons(this.getPlayerSlotFor(0, 3), this.getPlayerSlotFor(1, 3), this.getPlayerSlotFor(2, 3))
            .elements(this.lookupRegistry(Registries.TRIM_PATTERN).stream().toList())
            .drawFunction(pattern -> JSSTElementBuilder.from(pattern.templateItem().value())
                    .setName(pattern.description())
                    .hideDefaultTooltip()
                    .leftClick(Translations.select(), () -> {
                        Sounds.UI.click(player);
                        this.setPattern(pattern);
                        this.refresh();
                    }).build())
            .build();

    private final GridPaginator<TrimMaterial> materialPages = GridPaginator.<TrimMaterial>builder(this)
            .slots(UIRegion.playerRectangle(this, 4, 0, 7, 3))
            .fullButtons(this.getPlayerSlotFor(4, 3), this.getPlayerSlotFor(5, 3), this.getPlayerSlotFor(6, 3))
            .elements(this.lookupRegistry(Registries.TRIM_MATERIAL).stream().toList())
            .drawFunction(material -> JSSTElementBuilder.from(material.ingredient().value())
                    .setName(material.description())
                    .leftClick(Translations.select(), () -> {
                        Sounds.UI.click(player);
                        this.setMaterial(material);
                        this.refresh();
                    }).build())
            .build();

    public ArmourTrimEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.armourTrim"), MenuType.SMITHING, true);
    }

    @Override
    protected void drawStatic() {
        UIRegion.playerColumn(this, 3).fillStack(CommonLabels::divider);
        UIRegion.playerColumn(this, 7).fillStack(CommonLabels::divider);

        this.setSlot(1, JSSTElementBuilder.from(this.session.getStack()).ui()
                .hideDefaultTooltip()
                .removeComponent(DataComponents.TRIM)
                .setComponent(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE));

        this.setPlayerSlot(8, 3, CommonLabels.cancel(this::cancel));
    }

    @Override
    protected void refresh() {
        this.drawPreview(3);

        if (this.stack.has(DataComponents.TRIM)) {
            ArmorTrim currentTrim = this.stack.get(DataComponents.TRIM);
            TrimPattern pattern = currentTrim.pattern().value();
            TrimMaterial material = currentTrim.material().value();

            this.materialPages.draw();
            this.patternPages.draw();

            this.setSlot(0, JSSTElementBuilder.from(pattern.templateItem().value())
                    .setName(pattern.description())
                    .hideDefaultTooltip());

            this.setSlot(2, JSSTElementBuilder.from(material.ingredient().value())
                    .setName(material.description())
                    .hideDefaultTooltip());

            this.setPlayerSlot(8, 0, JSSTElementBuilder.from(Items.GRINDSTONE).ui()
                    .leftClick(Translations.clear(), () -> {
                        Sounds.UI.grind(player);
                        this.stack.remove(DataComponents.TRIM);
                        this.refresh();
                    }));
        } else {
            this.clearPlayerSlot(8, 0);

            this.materialPages.fillDisabled();
            this.patternPages.fillDisabled();

            this.clearSlot(0);
            this.clearSlot(2);

            this.setPlayerSlot(8, 0, JSSTElementBuilder.from(Items.NETHER_STAR).ui()
                    .leftClick(Component.translatable("jsst.itemEditor.editor.armourTrim.addRandomTrim"), () -> {
                        Sounds.UI.click(player);

                        Optional<Holder.Reference<TrimMaterial>> material = this.lookupRegistry(Registries.TRIM_MATERIAL).getRandom(this.player.getRandom());
                        Optional<Holder.Reference<TrimPattern>> pattern = this.lookupRegistry(Registries.TRIM_PATTERN).getRandom(this.player.getRandom());

                        if (material.isPresent() && pattern.isPresent()) {
                            this.stack.set(DataComponents.TRIM, new ArmorTrim(material.get(), pattern.get()));
                        }

                        this.refresh();
                    }));
        }
    }

    private void setMaterial(TrimMaterial material) {
        ArmorTrim old = this.stack.get(DataComponents.TRIM);
        if (old == null) return;

        this.stack.set(DataComponents.TRIM, new ArmorTrim(
                this.lookupRegistry(Registries.TRIM_MATERIAL).wrapAsHolder(material),
                old.pattern()
        ));
    }

    private void setPattern(TrimPattern pattern) {
        ArmorTrim old = this.stack.get(DataComponents.TRIM);
        if (old == null) return;

        this.stack.set(DataComponents.TRIM, new ArmorTrim(
                old.material(),
                this.lookupRegistry(Registries.TRIM_PATTERN).wrapAsHolder(pattern)
        ));
    }
}

package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.pagination.GridPaginator;
import red.jackf.jsst.util.sgui.pagination.PageButtonStyle;

import java.util.Optional;
import java.util.function.Consumer;

public class TrimEditor extends GuiEditor {
    private static final int GUI_SLOTS = 4;
    private interface Slots {
        int PATTERN = 0;
        int INPUT = 1;
        int MATERIAL = 2;
        int OUTPUT = 3;
    }

    public static final EditorType TYPE = new EditorType(
            TrimEditor::new,
            true,
            stack -> stack.is(ItemTags.TRIMMABLE_ARMOR),
            TrimEditor::makeLabel
    );

    private static GuiElementBuilderInterface<?> makeLabel(EditorContext context) {
        RegistryAccess.Frozen access = context.server().registryAccess();

        ItemStack stack = Items.NETHERITE_CHESTPLATE.getDefaultInstance();

        Optional<Holder.Reference<TrimMaterial>> material = access.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(TrimMaterials.REDSTONE);
        Optional<Holder.Reference<TrimPattern>> pattern = access.registryOrThrow(Registries.TRIM_PATTERN).getRandom(RandomSource.create());
        if (material.isPresent() && pattern.isPresent()) {
            ArmorTrim.setTrim(access, stack, new ArmorTrim(material.get(), pattern.get()));
        }

        return GuiElementBuilder.from(stack)
                .setName(Component.translatable("jsst.itemEditor.trim"))
                .hideFlags();
    }

    private @Nullable ArmorTrim trim = null;

    private final GridTranslator materialSlots = GridTranslator.playerSlots(this, 0, 3, 0, 3);
    private final GridPaginator<Holder<TrimMaterial>> materialPaginator = GridPaginator.<Holder<TrimMaterial>>builder(this)
            .slots(materialSlots)
            .list(this.context.server().registryAccess().registryOrThrow(Registries.TRIM_MATERIAL).holders().map(ref -> (Holder<TrimMaterial>) ref).toList())
            .normalButtons(GUI_SLOTS + Util.slot(1, 3),
                    GUI_SLOTS + Util.slot(0, 3),
                    GUI_SLOTS + Util.slot(2, 3),
                    PageButtonStyle.ArrowDirection.HORIZONTAL)
            .drawFunc(material -> GuiElementBuilder.from(LabelMaps.TRIM_MATERIALS.getLabel(material))
                    .addLoreLine(Hints.leftClick(Translations.select()))
                    .setCallback(Inputs.leftClick(() -> {
                        Sounds.click(player);
                        this.trim = new ArmorTrim(material, this.trim.pattern());
                        this.redraw();
                    }))
                    .build())
            .build();

    private final GridTranslator patternSlots = GridTranslator.playerSlots(this, 4, 7, 0, 3);
    private final GridPaginator<Holder<TrimPattern>> patternPaginator = GridPaginator.<Holder<TrimPattern>>builder(this)
            .slots(patternSlots)
            .list(this.context.server().registryAccess().registryOrThrow(Registries.TRIM_PATTERN).holders().map(ref -> (Holder<TrimPattern>) ref).toList())
            .normalButtons(GUI_SLOTS + Util.slot(5, 3),
                    GUI_SLOTS + Util.slot(4, 3),
                    GUI_SLOTS + Util.slot(6, 3),
                    PageButtonStyle.ArrowDirection.HORIZONTAL)
            .drawFunc(pattern -> GuiElementBuilder.from(LabelMaps.TRIM_PATTERNS.getLabel(pattern))
                    .addLoreLine(Hints.leftClick(Translations.select()))
                    .setCallback(Inputs.leftClick(() -> {
                        Sounds.click(player);
                        this.trim = new ArmorTrim(this.trim.material(), pattern);
                        this.redraw();
                    }))
                    .build())
            .build();

    public TrimEditor(ServerPlayer player, EditorContext context, ItemStack initial, Consumer<ItemStack> callback) {
        super(MenuType.SMITHING, player, context, initial, callback, true);
        this.setTitle(Component.translatable("jsst.itemEditor.trim"));
        this.loadFromStack();

        this.drawStatic();
    }

    private ItemStack removeTrim(ItemStack stack) {
        stack.removeTagKey(ArmorTrim.TAG_TRIM_ID);
        return stack;
    }

    private void loadFromStack() {
        this.trim = ArmorTrim.getTrim(this.context.server().registryAccess(), this.stack, true).orElse(null);
    }

    @Override
    protected void reset() {
        super.reset();
        this.loadFromStack();
    }

    private void buildOutput() {
        if (this.trim != null) {
            ArmorTrim.setTrim(this.context.server().registryAccess(), this.stack, this.trim);
        } else {
            removeTrim(this.stack);
        }
    }

    private Optional<ArmorTrim> makeRandomTrim() {
        var random = RandomSource.create();
        var material = this.context.server().registryAccess().registryOrThrow(Registries.TRIM_MATERIAL).getRandom(random);
        var pattern = this.context.server().registryAccess().registryOrThrow(Registries.TRIM_PATTERN).getRandom(random);

        if (material.isEmpty() || pattern.isEmpty()) return Optional.empty();

        return Optional.of(new ArmorTrim(material.get(), pattern.get()));
    }

    private void drawStatic() {
        this.setSlot(Slots.INPUT, removeTrim(getInitial()));

        for (int row = 0; row < 4; row++) {
            this.setSlot(GUI_SLOTS + Util.slot(3, row), CommonLabels.divider());
            this.setSlot(GUI_SLOTS + Util.slot(7, row), CommonLabels.divider());
        }

        this.setSlot(GUI_SLOTS + Util.slot(8, 3), CommonLabels.cancel(this::cancel));
    }

    @Override
    protected void redraw() {
        this.buildOutput();
        this.drawPreview(Slots.OUTPUT);

        final int addOrRemoveSlot = GUI_SLOTS + Util.slot(8, 0);
        if (this.trim == null) { // no trim
            var randomTrim = makeRandomTrim();
            if (randomTrim.isPresent()) {
                var stack = getInitial();
                ArmorTrim.setTrim(this.context.server().registryAccess(), stack, randomTrim.get());
                this.setSlot(addOrRemoveSlot, GuiElementBuilder.from(stack)
                        .hideFlags()
                        .setName(Hints.leftClick(Component.translatable("jsst.itemEditor.trim.add")))
                        .setCallback(Inputs.leftClick(() -> {
                            Sounds.click(player);
                            this.trim = randomTrim.get();
                            this.redraw();
                        })));
            } else {
                this.clearSlot(addOrRemoveSlot);
            }

            this.clearSlot(Slots.MATERIAL);
            this.clearSlot(Slots.PATTERN);

            GridTranslator.playerSlots(this, 0, 3, 0, 4).fill(this, CommonLabels.disabled().getItemStack());
            GridTranslator.playerSlots(this, 4, 7, 0, 4).fill(this, CommonLabels.disabled().getItemStack());
        } else { // yes trim
            this.setSlot(addOrRemoveSlot, GuiElementBuilder.from(Items.GRINDSTONE.getDefaultInstance())
                    .setName(Hints.leftClick(Component.translatable("jsst.itemEditor.trim.remove")))
                    .setCallback(Inputs.leftClick(() -> {
                        Sounds.clear(player);
                        this.trim = null;
                        this.redraw();
                    })));

            this.setSlot(Slots.MATERIAL, LabelMaps.TRIM_MATERIALS.getLabel(this.trim.material()));
            this.setSlot(Slots.PATTERN, LabelMaps.TRIM_PATTERNS.getLabel(this.trim.pattern()));

            this.materialPaginator.draw();
            this.patternPaginator.draw();
        }
    }
}

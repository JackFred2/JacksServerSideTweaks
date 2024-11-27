package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.ListPaginator;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMaps;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class EnchantmentEditor extends GuiEditor {
    public static final Type<EnchantmentEditor> TYPE = Editor.<EnchantmentEditor>typeBuilder(JSST.id("enchantment"))
            .factory(EnchantmentEditor::new)
            .labelFactory(EnchantmentEditor::getLabel)
            .build();

    private static GuiElementInterface getLabel(EditSession session) {
        return JSSTElementBuilder.from(Items.ENCHANTED_BOOK).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.enchantment"))
                .build();
    }

    private final List<EnchantmentInstance> enchantments = new ArrayList<>();

    private final ListPaginator<EnchantmentInstance> paginator = ListPaginator.<EnchantmentInstance>builder(this)
            .slots(UIRegion.rectangle(this,4, 0, 9, 6))
            .elements(enchantments)
            .drawFunction(this::drawRow)
            .modifiable(this::getRandom,
                    false,
                    20,
                    this::refresh)
            .build();

    private boolean hasEnchantment(Holder<Enchantment> enchantment) {
        return this.enchantments.stream()
                .anyMatch(instance -> instance.enchantment.equals(enchantment));
    }

    private EnchantmentInstance getRandom() {
        Registry<Enchantment> reg = RegistryUtils.lookup(this.session.registries(), Registries.ENCHANTMENT);

        var selected = reg.stream().map(reg::wrapAsHolder)
                .min(Comparator.<Holder<Enchantment>>comparingInt(e -> hasEnchantment(e) ? 1 : -1)
                        .thenComparingInt(e -> {
                            if (e.value().isPrimaryItem(this.stack)) {
                                return 0;
                            } else if (e.value().isSupportedItem(this.stack)) {
                                return 1;
                            } else {
                                return 2;
                            }
                        })).orElseThrow(() -> new IllegalArgumentException("No Enchantments!"));

        return new EnchantmentInstance(selected, selected.value().getMaxLevel());
    }

    private List<GuiElementInterface> drawRow(int index, EnchantmentInstance instance) {
        return List.of(
                JSSTElementBuilder.from(LabelMaps.ENCHANTMENT.apply(instance.enchantment)).ui()
                        .setName(Enchantment.getFullname(instance.enchantment, instance.level))
                        .leftClick(Translations.change(), () -> {
                            Sounds.UI.click(player);

                            Registry<Enchantment> reg = RegistryUtils.lookup(this.session.registries(), Registries.ENCHANTMENT);

                            InputMenus.<Holder<Enchantment>>selection(player)
                                    .title(Component.translatable("jsst.itemEditor.editor.enchantment.selectEnchantment"))
                                    .labelStacks(LabelMaps.ENCHANTMENT)
                                    .options(RegistryUtils.stream(reg).filter(ref -> !hasEnchantment(ref) || ref.value().equals(instance.enchantment.value())))
                                    .start(opt -> {
                                        opt.ifPresent(enchantmentHolder -> this.enchantments.set(index, new EnchantmentInstance(enchantmentHolder, enchantmentHolder.value().getMaxLevel())));

                                        this.open();
                                    });
                        })
                        .build(),
                JSSTElementBuilder.from(Items.EXPERIENCE_BOTTLE).ui()
                        .setName(Component.translatable("jsst.itemEditor.editor.enchantment.level", instance.level))
                        .setCount(instance.level)
                        .leftClick(Translations.change(), () -> {
                            Sounds.UI.click(player);

                            InputMenus.integer(player, 1, 255)
                                    .title(Component.translatable("jsst.itemEditor.editor.enchantment.setLevel"))
                                    .initial(String.valueOf(instance.level))
                                    .start(opt -> {
                                        opt.ifPresent(integer -> this.enchantments.set(index, new EnchantmentInstance(instance.enchantment, integer)));

                                        this.open();
                                    });
                        })
                        .rightClick(Component.translatable("jsst.itemEditor.editor.enchantment.setMaximum", instance.enchantment.value().getMaxLevel()), () -> {
                            Sounds.UI.click(player);

                            this.enchantments.set(index, new EnchantmentInstance(instance.enchantment, instance.enchantment.value().getMaxLevel()));

                            this.refresh();
                        })
                        .build()
        );
    }

    public EnchantmentEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.enchantment"), MenuType.GENERIC_9x6, false);

        this.loadEnchantments();
    }

    private DataComponentType<ItemEnchantments> getComponentKey() {
        return this.stack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    private void loadEnchantments() {
        this.enchantments.clear();

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : this.stack.getOrDefault(getComponentKey(), ItemEnchantments.EMPTY).entrySet()) {
            this.enchantments.add(new EnchantmentInstance(entry.getKey(), entry.getIntValue()));
        }
    }

    @Override
    protected ItemStack buildOutput() {
        ItemStack stack = super.buildOutput();

        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        for (EnchantmentInstance i : this.enchantments) {
            enchantments.set(i.enchantment, i.level);
        }

        stack.set(getComponentKey(), enchantments.toImmutable());

        return stack;
    }

    @Override
    protected void onReset() {
        this.loadEnchantments();
    }

    @Override
    protected void drawStatic() {
        UIRegion.column(this, 3).fillElement(CommonElements::divider);

        this.setSlot(0, 5, CommonElements.cancel(this::cancel));
    }

    @Override
    protected void refresh() {
        this.drawPreview(1, 1);

        this.paginator.draw();
    }

    private record EnchantmentInstance(Holder<Enchantment> enchantment, int level) {}
}

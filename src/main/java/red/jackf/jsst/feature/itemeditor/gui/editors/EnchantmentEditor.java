package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.Util;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.elements.ToggleButton;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.menus.selector.SelectorMenu;
import red.jackf.jsst.util.sgui.pagination.ListPaginator;

import java.util.*;
import java.util.function.Consumer;

public class EnchantmentEditor extends GuiEditor {
    public static final EditorType TYPE = new EditorType(
            JSST.id("enchantment"),
            EnchantmentEditor::new,
            false,
            false,
            stack -> true,
            context -> JSSTElementBuilder.from(Items.ENCHANTING_TABLE)
                    .setName(Component.translatable("jsst.itemEditor.enchantment"))
    );

    private final List<EnchantmentInstance> enchantments = new ArrayList<>();
    private final ListPaginator<EnchantmentInstance> paginator = ListPaginator.<EnchantmentInstance>builder(this)
            .max(50)
            .onUpdate(this::redraw)
            .list(this.enchantments)
            .slots(4, 9, 0, 6)
            .modifiable(() -> EnchantmentInstance.makeRandom(this.stack, this.context.server().registryAccess()), false)
            .rowDraw(this::drawRow)
            .build();

    public EnchantmentEditor(ServerPlayer player, EditorContext context, ItemStack initial, Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, context, initial, callback, false);
        this.setTitle(Component.translatable("jsst.itemEditor.enchantment"));
        this.drawStatic();

        this.loadFromStack();
    }

    private List<GuiElementInterface> drawRow(int index, EnchantmentInstance instance) {
        GuiElement enchantment = JSSTElementBuilder.ui(LabelMaps.ENCHANTMENTS.getLabel(instance.enchantment))
                .setName(instance.enchantment.getFullname(instance.level))
                .leftClick(Component.translatable("jsst.itemEditor.enchantment.setEnchantment"), () -> {
                    Sounds.click(player);
                    var options = this.context.server()
                            .registryAccess()
                            .registryOrThrow(Registries.ENCHANTMENT)
                            .entrySet()
                            .stream()
                            .sorted(Comparator.comparing(e -> e.getKey().location()))
                            .map(Map.Entry::getValue)
                            .toList();

                    SelectorMenu.<Enchantment>builder(player)
                            .title(Component.translatable("jsst.itemEditor.enchantment.setEnchantment"))
                            .options(options)
                            .labelMap(LabelMaps.ENCHANTMENTS)
                            .filter(() -> ToggleButton.builder()
                                            .label(Component.translatable("jsst.itemEditor.enchantment.showApplicableOnly"))
                                            .disabled(Items.NETHER_STAR.getDefaultInstance())
                                            .enabled(JSSTElementBuilder.from(this.stack.copy()).hideFlags().asStack()),
                                    ench -> ench.canEnchant(this.stack),
                                    true)
                            .createAndShow(result -> {
                                if (result.hasResult()) {
                                    this.enchantments.set(index, new EnchantmentInstance(result.result(), result.result().getMaxLevel()));
                                }
                                this.open();
                            });
                }).build();

        GuiElement level = JSSTElementBuilder.ui(Items.BOOKSHELF)
                .setCount(Mth.clamp(instance.level, 1, 64))
                .leftClick(Component.translatable("jsst.itemEditor.enchantment.setLevel"), () -> {
                    Sounds.click(player);

                    Menus.integer(player,
                            Component.translatable("jsst.itemEditor.enchantment.setLevel"),
                            instance.level,
                            1,
                            255,
                            null,
                            result -> {
                                if (result.hasResult()) {
                                    this.enchantments.set(index, new EnchantmentInstance(instance.enchantment, result.result()));
                                }
                                this.open();
                            });
                }).build();

        return List.of(enchantment, level);
    }

    private void loadFromStack() {
        this.enchantments.clear();
        this.enchantments.addAll(EnchantmentHelper.getEnchantments(this.stack).entrySet().stream()
                .map(entry -> new EnchantmentInstance(entry.getKey(), entry.getValue()))
                .toList());
    }

    private void applyToStack() {
        var map = new LinkedHashMap<Enchantment, Integer>(this.enchantments.size());

        for (EnchantmentInstance instance : this.enchantments)
            map.put(instance.enchantment, instance.level);

        if (stack.is(Items.ENCHANTED_BOOK)) stack.removeTagKey(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
        EnchantmentHelper.setEnchantments(map, stack);
    }

    @Override
    protected void onReset() {
        this.loadFromStack();
    }

    private void drawStatic() {
        this.setSlot(Util.slot(0, 5), CommonLabels.cancel(this::cancel));

        for (int row = 0; row < 6; row++)
            this.setSlot(Util.slot(3, row), CommonLabels.divider());
    }

    @Override
    protected void redraw() {
        this.applyToStack();
        this.drawPreview(Util.slot(1, 1));

        this.paginator.draw();
    }

    public record EnchantmentInstance(Enchantment enchantment, int level) {
        public static EnchantmentInstance makeRandom(ItemStack stack, RegistryAccess.Frozen registries) {
            var available = registries.registryOrThrow(Registries.ENCHANTMENT).stream()
                    .filter(ench2 -> ench2.canEnchant(stack))
                    .toList();
            Enchantment ench = available.isEmpty() ?
                    Enchantments.VANISHING_CURSE :
                    available.get(RandomSource.create().nextInt(available.size()));
            return new EnchantmentInstance(ench, ench.getMaxLevel());
        }
    }


}

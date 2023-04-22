package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EnchantmentEditor extends Editor {
    private static final int MAX_ENCHANTMENTS = 20;

    private List<EnchantmentInstance> enchantments;
    private int page = 0;

    public EnchantmentEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        loadEnchantments();
    }

    private void loadEnchantments() {
        this.enchantments = EnchantmentHelper.getEnchantments(this.stack).entrySet().stream()
                .map(entry -> new EnchantmentInstance(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.ENCHANTED_BOOK).withName("Edit Enchantments").build();
    }

    private void reset() {
        Sounds.clear(player);
        this.stack = getOriginal();
        loadEnchantments();
        open();
    }

    private ItemStack build() {
        stack.removeTagKey(ItemStack.TAG_ENCH);
        stack.removeTagKey("StoredEnchantments");
        EnchantmentHelper.setEnchantments(enchantments.stream()
                .collect(Collectors.<EnchantmentInstance, Enchantment, Integer>toMap(EnchantmentInstance::enchantment, EnchantmentInstance::level, Integer::sum)), stack);
        return stack;
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(Labels.create(build()).withHint("Click to finish").keepLore().build(), () -> {
            stack = build();
            complete();
        }));

        elements.put(36, new ItemGuiElement(Labels.create(Items.LECTERN).withName("Add Fake Lore").withHint("Generates fake enchantment tooltips, and disables vanilla enchantment tooltip").withHint("Fixes tooltips for enchantments > 10").build(), () -> {
            Sounds.write(player);
            stack.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
            var newLore = enchantments.stream().map(ench -> (Component) ench.getText().withStyle(ChatFormatting.GRAY)).toList();
            stack = LoreEditor.mergeLore(stack, newLore);
            open();
        }));
        elements.put(45, new ItemGuiElement(Labels.create(Items.GRINDSTONE).withName("Clear").build(), () -> {
            Sounds.grind(player);
            this.enchantments.clear();
            open();
        }));
        elements.put(46, EditorUtils.reset(this::reset));
        elements.put(47, EditorUtils.cancel(this::cancel));

        // Divider
        for (int i = 3; i < 54; i += 9)
            elements.put(i, EditorUtils.divider());

        var maxPage = (enchantments.size() / 5) - (enchantments.size() >= MAX_ENCHANTMENTS ? 1 : 0);
        this.page = Mth.clamp(this.page, 0, maxPage);
        EditorUtils.drawPage(elements, enchantments, page, maxPage, newPage -> {
            Sounds.interact(player, 1f + ((float) (newPage + 1) / (maxPage + 1)) / 2);
            this.page = newPage;
            open();
        }, 6, (slot, index) -> {
            var instance = enchantments.get(index);
            EnchantmentHelper.setEnchantments(Map.of(instance.enchantment, instance.level), stack);
            var stack = LabelData.ENCHANTMENTS.get(instance.enchantment);
            var label = Labels.create(stack).withName(instance.getText()).withHint(instance.level.toString()).build();
            elements.put(slot, new ItemGuiElement(label, () -> {
                Sounds.interact(player);
                Menus.enchantment(player, CancellableCallback.of(ench -> {
                    Sounds.success(player);
                    enchantments.set(index, new EnchantmentInstance(ench, instance.level));
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));
            elements.put(slot + 1, new ItemGuiElement(Labels.create(Items.EXPERIENCE_BOTTLE).withName("Set Level").build(), () -> {
                Sounds.interact(player);
                Menus.string(player, instance.level.toString(), CancellableCallback.of(newLevel -> {
                    try {
                        var parsed = Integer.parseUnsignedInt(newLevel);
                        if (parsed == 0) {
                            enchantments.remove((int) index);
                        } else {
                            enchantments.set(index, new EnchantmentInstance(instance.enchantment, Math.min(parsed, 255)));
                        }
                        Sounds.success(player);
                        open();
                    } catch (NumberFormatException e) {
                        Sounds.error(player);
                        open();
                    }
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));
        }, index -> {
            Sounds.grind(player);
            enchantments.remove((int) index);
            open();
        }, () -> Menus.enchantment(player, CancellableCallback.of(ench -> {
            Sounds.success(player);
            enchantments.add(new EnchantmentInstance(ench, ench.getMaxLevel()));
            open();
        }, () -> {
            Sounds.error(player);
            open();
        })));

        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Enchantments"), elements));
    }

    private record EnchantmentInstance(Enchantment enchantment, Integer level) {
        private MutableComponent getText() {
            var text = Component.translatable(enchantment.getDescriptionId())
                    .setStyle(enchantment.isCurse() ? Labels.CLEAN.withColor(ChatFormatting.RED) : Labels.CLEAN);
            if (level != 1 && enchantment.getMaxLevel() != 1)
                text.append(CommonComponents.SPACE).append(Component.literal(getNumeral()).setStyle(Style.EMPTY));
            return text;
        }

        private static final Map<Integer, String> NUMERALS = new LinkedHashMap<>();

        static {
            NUMERALS.put(100, "C");
            NUMERALS.put(90, "XC");
            NUMERALS.put(50, "L");
            NUMERALS.put(40, "XL");
            NUMERALS.put(10, "X");
            NUMERALS.put(9, "IX");
            NUMERALS.put(5, "V");
            NUMERALS.put(4, "IV");
            NUMERALS.put(1, "I");
        }

        private String getNumeral() {
            var count = level;
            var itercount = 0;
            StringBuilder s = new StringBuilder();
            while (count > 0) {
                itercount++;
                for (var n : NUMERALS.entrySet()) {
                    if (count >= n.getKey()) {
                        s.append(n.getValue());
                        count -= n.getKey();
                        break;
                    }
                }
                if (itercount > 10) break;
            }
            return s.toString();
        }
    }
}

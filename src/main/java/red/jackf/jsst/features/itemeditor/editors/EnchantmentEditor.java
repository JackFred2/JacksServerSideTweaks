package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EnchantmentEditor extends Editor {
    private static final int MAX_ENCHANTMENTS = 20;
    private static final Map<Enchantment, ItemStack> ICONS = new LinkedHashMap<>();

    static {
        ICONS.put(Enchantments.ALL_DAMAGE_PROTECTION, Labels.create(Items.SHIELD).build());
        ICONS.put(Enchantments.FIRE_PROTECTION, Labels.create(Items.FIRE_CHARGE).build());
        ICONS.put(Enchantments.FALL_PROTECTION, Labels.create(Items.FEATHER).build());
        ICONS.put(Enchantments.BLAST_PROTECTION, Labels.create(Items.TNT).build());
        ICONS.put(Enchantments.PROJECTILE_PROTECTION, Labels.create(Items.ARROW).build());
        ICONS.put(Enchantments.RESPIRATION, Labels.create(Items.WATER_BUCKET).build());
        ICONS.put(Enchantments.AQUA_AFFINITY, Labels.create(Items.CONDUIT).build());
        ICONS.put(Enchantments.THORNS, Labels.create(Items.CACTUS).build());
        ICONS.put(Enchantments.DEPTH_STRIDER, Labels.create(Items.COD).build());
        ICONS.put(Enchantments.FROST_WALKER, Labels.create(Items.ICE).build());
        ICONS.put(Enchantments.BINDING_CURSE, Labels.create(Items.CHAIN).build());
        ICONS.put(Enchantments.SOUL_SPEED, Labels.create(Items.SOUL_SOIL).build());
        ICONS.put(Enchantments.SWIFT_SNEAK, Labels.create(Items.LEATHER_BOOTS).build());
        ICONS.put(Enchantments.SHARPNESS, Labels.create(Items.IRON_SWORD).build());
        ICONS.put(Enchantments.SMITE, Labels.create(Items.ZOMBIE_HEAD).build());
        ICONS.put(Enchantments.BANE_OF_ARTHROPODS, Labels.create(Items.SPIDER_EYE).build());
        ICONS.put(Enchantments.KNOCKBACK, Labels.create(Items.PISTON).build());
        ICONS.put(Enchantments.FIRE_ASPECT, Labels.create(Items.FLINT_AND_STEEL).build());
        ICONS.put(Enchantments.MOB_LOOTING, Labels.create(Items.LAPIS_LAZULI).build());
        ICONS.put(Enchantments.SWEEPING_EDGE, Labels.create(Items.IRON_HOE).build());
        ICONS.put(Enchantments.BLOCK_EFFICIENCY, Labels.create(Items.SUGAR).build());
        ICONS.put(Enchantments.SILK_TOUCH, Labels.create(Items.WHITE_WOOL).build());
        ICONS.put(Enchantments.UNBREAKING, Labels.create(Items.NETHERITE_BLOCK).build());
        ICONS.put(Enchantments.BLOCK_FORTUNE, Labels.create(Items.LAPIS_BLOCK).build());
        ICONS.put(Enchantments.POWER_ARROWS, Labels.create(Items.CROSSBOW).build());
        ICONS.put(Enchantments.PUNCH_ARROWS, Labels.create(Items.PISTON).build());
        ICONS.put(Enchantments.FLAMING_ARROWS, Labels.create(Items.FLINT_AND_STEEL).build());
        ICONS.put(Enchantments.INFINITY_ARROWS, Labels.create(Items.SPECTRAL_ARROW).build());
        ICONS.put(Enchantments.FISHING_LUCK, Labels.create(Items.ENCHANTED_BOOK).build());
        ICONS.put(Enchantments.FISHING_SPEED, Labels.create(Items.SUGAR).build());
        ICONS.put(Enchantments.LOYALTY, Labels.create(Items.ENDER_EYE).build());
        ICONS.put(Enchantments.IMPALING, Labels.create(Items.ARROW).build());
        ICONS.put(Enchantments.RIPTIDE, Labels.create(Items.NAUTILUS_SHELL).build());
        ICONS.put(Enchantments.CHANNELING, Labels.create(Items.LIGHTNING_ROD).build());
        ICONS.put(Enchantments.MULTISHOT, Labels.create(new ItemStack(Items.ARROW, 5)).build());
        ICONS.put(Enchantments.QUICK_CHARGE, Labels.create(Items.SUGAR).build());
        ICONS.put(Enchantments.PIERCING, Labels.create(Items.ARROW).build());
        ICONS.put(Enchantments.MENDING, Labels.create(Items.EMERALD).build());
        ICONS.put(Enchantments.VANISHING_CURSE, Labels.create(Items.ENDER_PEARL).build());
    }

    private static final ItemStack DEFAULT_ICON = new ItemStack(Items.ENCHANTED_BOOK);

    private static ItemStack getEnchantmentStack(Enchantment enchantment) {
        return ICONS.getOrDefault(enchantment, DEFAULT_ICON).copy();
    }


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
        var stack = getOriginal();
        EnchantmentHelper.setEnchantments(enchantments.stream()
                .collect(Collectors.<EnchantmentInstance, Enchantment, Integer>toMap(EnchantmentInstance::enchantment, EnchantmentInstance::level, Integer::sum)), stack);
        return stack;
    }

    private void enchantSelector(CancellableCallback<Enchantment> callback) {
        var icons = new LinkedHashMap<Enchantment, ItemStack>();
        BuiltInRegistries.ENCHANTMENT.forEach(e -> icons.put(e, getEnchantmentStack(e).setHoverName(Component.translatable(e.getDescriptionId())
                .withStyle(Labels.CLEAN))));
        Menus.selector(player, icons, callback);
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(Labels.create(build()).withHint("Click to finish").keepLore()
                .build(), () -> {
            stack = build();
            complete();
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

        this.page = Mth.clamp(this.page, 0, enchantments.size() / 5);
        var maxPage = (enchantments.size() / 5) - (enchantments.size() == MAX_ENCHANTMENTS ? 1 : 0);
        EditorUtils.drawPage(elements, enchantments, page, maxPage, newPage -> {
            Sounds.interact(player, 1f + ((float) (newPage + 1) / (maxPage + 1)) / 2);
            this.page = newPage;
            open();
        }, (slot, index) -> {
            var instance = enchantments.get(index);
            EnchantmentHelper.setEnchantments(Map.of(instance.enchantment, instance.level), stack);
            var stack = getEnchantmentStack(instance.enchantment);
            var label = Labels.create(stack).withName(instance.getText()).withHint(instance.level.toString()).build();
            elements.put(slot, new ItemGuiElement(label, () -> enchantSelector(CancellableCallback.of(ench -> {
                Sounds.success(player);
                enchantments.set(index, new EnchantmentInstance(ench, instance.level));
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }))));
            elements.put(slot + 1, new ItemGuiElement(Labels.create(Items.EXPERIENCE_BOTTLE).withName("Set Level")
                    .build(), () -> Menus.string(player, instance.level.toString(), newLevel -> {
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
                    })));
        }, index -> {
            Sounds.grind(player);
            enchantments.remove((int) index);
            open();
        }, () -> enchantSelector(CancellableCallback.of(ench -> {
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
        private Component getText() {
            var text = Component.translatable(enchantment.getDescriptionId())
                    .setStyle(enchantment.isCurse() ? Labels.CLEAN.withColor(ChatFormatting.RED) : Labels.CLEAN);
            if (level != 1 && enchantment.getMaxLevel() != 1)
                text.append(CommonComponents.SPACE).append(Component.literal(getNumeral()).setStyle(Labels.CLEAN));
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

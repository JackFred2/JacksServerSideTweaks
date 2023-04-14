package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.editors.LoreEditor;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.*;
import java.util.stream.Collectors;

public class LoreVisiblityMenu {
    private static final Map<ItemStack.TooltipPart, ItemStack> ICONS = new LinkedHashMap<>();
    static {
        ICONS.put(ItemStack.TooltipPart.ENCHANTMENTS, Labels.create(Items.ENCHANTING_TABLE).withName("Enchantments").withHint("Mask: " + ItemStack.TooltipPart.ENCHANTMENTS.getMask()).build());
        ICONS.put(ItemStack.TooltipPart.MODIFIERS, Labels.create(Items.ANVIL).withName("Attributes").withHint("Mask: " + ItemStack.TooltipPart.MODIFIERS.getMask()).build());
        ICONS.put(ItemStack.TooltipPart.UNBREAKABLE, Labels.create(Items.NETHERITE_BLOCK).withName("Unbreakable").withHint("Mask: " + ItemStack.TooltipPart.UNBREAKABLE.getMask()).build());
        ICONS.put(ItemStack.TooltipPart.CAN_DESTROY, Labels.create(Items.DIAMOND_PICKAXE).withName("Destroyable Blocks").withHint("Mask: " + ItemStack.TooltipPart.CAN_DESTROY.getMask()).build());
        ICONS.put(ItemStack.TooltipPart.CAN_PLACE, Labels.create(Items.OAK_PLANKS).withName("Placeable on Blocks").withHint("Mask: " + ItemStack.TooltipPart.CAN_PLACE.getMask()).build());
        ICONS.put(ItemStack.TooltipPart.ADDITIONAL, Labels.create(Items.SUSPICIOUS_STEW).withName("Additional").withHint("Potion Effects, Book Information").withHint("Enchantments on Enchanted Books, Firework Info,").withHint("Map Tooltips and Bundle Items").withHint("Mask: " + ItemStack.TooltipPart.ADDITIONAL.getMask()).build());
        ICONS.put(ItemStack.TooltipPart.DYE, Labels.create(Items.PINK_DYE).withName("Dye").withHint("Mask: " + ItemStack.TooltipPart.DYE.getMask()).build());
        ICONS.put(ItemStack.TooltipPart.UPGRADES, Labels.create(Items.IRON_CHESTPLATE).withName("Armour Upgrades").withHint("Mask: " + ItemStack.TooltipPart.UPGRADES.getMask()).build());
    }

    private final ServerPlayer player;
    private final ItemStack stack;
    private final CancellableCallback<ItemStack> callback;

    private static ItemStack getIcon(ItemStack.TooltipPart part) {
        return ICONS.getOrDefault(part, new ItemStack(Items.NETHER_STAR));
    }

    private static void showTooltipPart(ItemStack stack, ItemStack.TooltipPart part) {
        //noinspection DataFlowIssue
        if (!stack.hasTag() || !stack.getTag().contains("HideFlags")) return;
        stack.getTag().putInt("HideFlags", stack.getTag().getInt("HideFlags") & (255 ^ part.getMask()));
    }

    private static Set<ItemStack.TooltipPart> getHidden(ItemStack stack) {
        var flags = stack.getHideFlags();
        return Arrays.stream(ItemStack.TooltipPart.values()).filter(part -> (part.getMask() & flags) != 0).collect(Collectors.toSet());
    }

    protected LoreVisiblityMenu(ServerPlayer player, ItemStack stack, CancellableCallback<ItemStack> callback) {
        this.player = player;
        this.stack = stack;
        this.callback = callback;
    }

    protected void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(0, new ItemGuiElement(Labels.create(stack).withHint("Click to finish").keepLore().build(), () -> callback.accept(stack)));

        var hidden = getHidden(stack);
        var i = 0;
        for (ItemStack.TooltipPart part : ItemStack.TooltipPart.values()) {
            var icon = getIcon(part).copy();
            var isHidden = hidden.contains(part);
            if (isHidden) EnchantmentHelper.setEnchantments(Map.of(Enchantments.SILK_TOUCH, 1), icon);
            LoreEditor.mergeLore(stack, List.of(Component.literal(isHidden ? "Hidden" : "Shown").withStyle(Labels.HINT)));
            var slot = (i % 7) + ((i / 7) * 9) + 2;
            elements.put(slot, new ItemGuiElement(icon, () -> {
                if (hidden.contains(part)) { // remove part
                    Sounds.success(player);
                    showTooltipPart(stack, part);
                    open();
                } else { // add part
                    Sounds.error(player);
                    stack.hideTooltipPart(part);
                    open();
                }
            }));
            i++;
        }

        elements.put(17, EditorUtils.cancel(callback::cancel));

        player.openMenu(EditorUtils.make9x2(Component.literal("Editing Vanilla Lore Components"), elements));
    }
}

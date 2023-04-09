package red.jackf.jsst.features.itemeditor;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static net.minecraft.network.chat.Component.literal;

public class EditSession {
    private final CommandSourceStack source;
    private final ServerPlayer player;
    private final ItemStack stack;

    public EditSession(CommandSourceStack source, ServerPlayer player, ItemStack stack) {
        this.source = source;
        this.player = player;
        this.stack = stack;
    }

    private static ItemStack label(ItemStack stack, String text) {
        stack.setHoverName(Component.literal(text).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false)));
        for (ItemStack.TooltipPart part : ItemStack.TooltipPart.values())
            stack.hideTooltipPart(part);
        return stack;
    }

    private void mainMenu() {
        var buttons = new HashMap<Integer, EditorUtils.ItemButton>();
        buttons.put(10, new EditorUtils.ItemButton(stack.copy(), this::finish));

        for (var slot : new int[]{3,12,21}) { // divider
            buttons.put(slot, new EditorUtils.ItemButton(EditorUtils.DIVIDER.copy(), null));
        }

        var features = new ArrayList<EditorUtils.ItemButton>();
        features.add(new EditorUtils.ItemButton(label(new ItemStack(Items.NAME_TAG), "Edit Name"), this::editName));
        features.add(new EditorUtils.ItemButton(label(new ItemStack(Items.WRITABLE_BOOK), "Edit Lore"), this::editLore));
        features.add(new EditorUtils.ItemButton(label(new ItemStack(Items.LAPIS_LAZULI), "Edit Enchantments"), this::editEnchantments));
        if (stack.getItem() instanceof PotionItem || stack.is(Items.TIPPED_ARROW))
            features.add(new EditorUtils.ItemButton(label(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION), "Edit Potion Effects"), this::editPotionEffects));

        for (int i = 0; i < features.size(); i++) { // 3x5 area on the right side
            var row = i / 5;
            var column = 4 + (i % 5);
            buttons.put(row * 9 + column, features.get(i));
        }

        if (player.openMenu(EditorUtils.makeMenu(literal("Item Editor"), buttons)).isEmpty())
            source.sendFailure(CommandUtils.line(CommandUtils.TextType.ERROR, CommandUtils.text("Error creating main menu")));
    }

    private void editPotionEffects() {
        JSST.LOGGER.info("Editing potion effects");
    }

    private void editName() {
        JSST.LOGGER.info("Editing name");
    }

    private void editLore() {
        JSST.LOGGER.info("Editing lore");
    }

    private void editEnchantments() {
        JSST.LOGGER.info("Editing enchantments");
    }

    private void finish() {
        player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS,1f, 1f);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        player.closeContainer();
    }

    public void start() {
        mainMenu();
    }
}

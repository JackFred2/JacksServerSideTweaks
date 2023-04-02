package red.jackf.jsst.features.itemeditor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.network.chat.Component.literal;

public class EditSession {
    private static final Style TITLE = Style.EMPTY.withBold(true).withColor(0x00137F);
    private static final Style HEADER = Style.EMPTY.withUnderlined(true).withColor(0x00137F);
    private static final Style TECHNICAL = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
    private static final Style ERROR = Style.EMPTY.withColor(ChatFormatting.DARK_RED);
    private static final Style BUTTON = Style.EMPTY.withColor(0x2D5EFF);
    private static final Style RETURN = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final Style BASE = Style.EMPTY.withColor(ChatFormatting.BLACK)
            .withBold(false)
            .withItalic(false)
            .withObfuscated(false)
            .withUnderlined(false)
            .withStrikethrough(false)
            .withClickEvent(null)
            .withHoverEvent(null)
            .withInsertion(null);

    private static final Component BLANK_LINE = literal("");

    private static final Component RETURN_BUTTON = literal("←").withStyle(RETURN.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literal("Return to title page"))));

    private static final MenuProvider LECTERN_PROVIDER = new MenuProvider() {
        @Override
        @NotNull
        public Component getDisplayName() {
            return Component.empty();
        }

        @Override
        public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
            return new LecternMenu(i);
        }
    };

    private final ServerPlayer player;
    private ItemStack stack;

    private static ItemStack createFauxBook(ListTag pages) {
        var stack = new ItemStack(Items.WRITTEN_BOOK);
        var tag = stack.getOrCreateTag();
        tag.put("pages", pages);
        tag.put("author", StringTag.valueOf("JSST"));
        tag.put("title", StringTag.valueOf("JSST Item Editor"));
        return stack;
    }

    private static StringTag toPage(List<Component> lines) {
        if (lines.size() == 0) return StringTag.valueOf("");
        var text = literal("");
        text.append(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            text.append(literal("\n").withStyle(BASE));
            text.append(lines.get(i));
        }
        return StringTag.valueOf(Component.Serializer.toJson(text));
    }

    private StringTag createTitlePage(Map<StackFeature, Integer> otherPages) {
        var lines = new ArrayList<Component>();
        lines.add(literal("ITEM EDITOR").withStyle(TITLE));
        lines.add(literal(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).withStyle(BASE.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack)))));
        lines.add(BLANK_LINE);
        otherPages.forEach((feature, pageNo) -> lines.add(literal(feature.label).withStyle(HEADER.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(pageNo))))));
        return toPage(lines);
    }

    private static Component createTitle(String label, @Nullable String editCommand) {
        var base = literal("");
        base.append(RETURN_BUTTON);
        base.append(literal(" " + label + " ").withStyle(TITLE));
        if (editCommand != null) base.append(literal("[EDIT]").withStyle(BUTTON.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, editCommand))));
        return base;
    }

    private static StringTag createNamePage(ItemStack stack) {
        var lines = new ArrayList<Component>();
        lines.add(createTitle("NAME", "/jsst itemEditor editName"));
        lines.add(stack.getHoverName());
        return toPage(lines);
    }

    private static StringTag createLorePage(ItemStack stack) {
        var lines = new ArrayList<Component>();
        lines.add(createTitle("LORE", "/jsst itemEditor editLore"));
        return toPage(lines);
    }

    private static StringTag createEnchantmentsPage(ItemStack stack) {
        var lines = new ArrayList<Component>();
        lines.add(createTitle("ENCHANTMENTS", "/jsst itemEditor editEnchantments"));
        return toPage(lines);
    }

    private static StringTag createPotionEffects(ItemStack stack) {
        var lines = new ArrayList<Component>();
        lines.add(createTitle("POTION EFFECTS", "/jsst itemEditor editPotionEffects"));
        return toPage(lines);
    }

    private ItemStack createBook() {
        var pages = new ListTag();
        var page = 2;
        var otherPages = new LinkedHashMap<StackFeature, Integer>();
        for (var feature : StackFeature.values())
            if (feature.applies.test(stack)) otherPages.put(feature, page++);
        pages.add(createTitlePage(otherPages));
        otherPages.keySet().forEach(feature -> pages.add(feature.pageSupplier.apply(stack)));
        return createFauxBook(pages);
    }

    public EditSession(CommandSourceStack source, ItemStack stack) throws CommandSyntaxException {
        this.player = source.getPlayerOrException();
        this.stack = stack.copy();
    }

    public void start() {
        player.openMenu(LECTERN_PROVIDER);
        player.containerMenu.getSlot(0).set(createBook());
    }

    private enum StackFeature {
        NAME("Name", stack -> true, EditSession::createNamePage),
        LORE("Lore", stack -> true, EditSession::createLorePage),
        ENCHANTMENTS("Enchantments", stack -> true, EditSession::createEnchantmentsPage),
        POTION_EFFECTS("Potion Effects", stack -> stack.getItem() instanceof PotionItem || stack.is(Items.TIPPED_ARROW), EditSession::createPotionEffects);

        private final String label;
        private final Predicate<ItemStack> applies;
        private final Function<ItemStack, StringTag> pageSupplier;

        StackFeature(String label, Predicate<ItemStack> applies, Function<ItemStack, StringTag> pageSupplier) {
            this.label = label;
            this.applies = applies;
            this.pageSupplier = pageSupplier;
        }
    }
}

package red.jackf.jsst.features.itemeditor.editors;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.BannerPatternMenu;
import red.jackf.jsst.features.itemeditor.menus.ColourSwapMenu;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.*;
import java.util.function.Consumer;

public class BannerEditor extends Editor {
    private static final String PMC_BANNER_URL = "https://www.planetminecraft.com/banner/";
    private static final int MAX_PATTERNS = 16;
    private List<Pair<Holder<BannerPattern>, DyeColor>> patterns = new ArrayList<>();
    private DyeColor base = DyeColor.WHITE;
    private ItemType itemType = ItemType.BANNER;
    private ViewType viewType = ViewType.INDIVIDUAL;
    public BannerEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        parseStack(true);
    }

    private void parseStack(boolean changeShield) {
        if (stack.getItem() instanceof BannerItem || stack.getItem() == Items.SHIELD) {
            var tag = BlockItem.getBlockEntityData(stack);
            var isShield = stack.is(Items.SHIELD);
            this.base = isShield ? (tag != null ? DyeColor.byId(tag.getInt(ShieldItem.TAG_BASE_COLOR)) : DyeColor.WHITE) : ((BannerItem) stack.getItem()).getColor();
            var parsedPatterns = BannerBlockEntity.createPatterns(DyeColor.WHITE, BannerBlockEntity.getItemPatterns(stack));
            parsedPatterns.remove(0); // remove full 'blank' pattern
            this.patterns = parsedPatterns;
            if (changeShield) this.itemType = isShield ? ItemType.SHIELD : ItemType.BANNER;
        }
    }

    private ItemStack build() {
        return BannerUtils.builder(base).set(patterns).setShield(itemType == ItemType.SHIELD).mergeTag(stack.getTag()).build();
    }

    @Override
    public ItemStack label() {
        return Labels.create(BannerUtils.JSST_BANNER).withName("Edit Pattern").build();
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.getItem() instanceof BannerItem || stack.is(Items.SHIELD);
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        elements.put(0, new ItemGuiElement(Labels.create(build()).keepLore().withHint("Click to finish").build(), () -> {
            this.stack = build();
            complete();
        }));

        elements.put(5, new ItemGuiElement(Labels.create(BannerUtils.Builder.BY_COLOUR.get(base)).withName("Set Base Colour").build(), () -> Menus.selector(player, BannerUtils.Builder.ICONS, CancellableCallback.of(newColour -> {
            Sounds.success(player);
            this.base = newColour;
            open();
        }, () -> {
            Sounds.error(player);
            open();
        }))));

        elements.put(36, new ItemGuiElement(Labels.create(BannerUtils.PMC_BANNER).withName(Component.literal("Import ").withStyle(CommandUtils.CLEAN)
                .append(Component.literal("Planet").withStyle(ChatFormatting.GREEN))
                .append(Component.literal("Minecraft ").withStyle(Style.EMPTY.withColor(0xA3692B)))
                .append(Component.literal("Banner Code")))
                .withHint(PMC_BANNER_URL)
                .withHint("- Paste the code at the end of the 'Shareable URL',")
                .withHint("  after the '?b='.")
                .withHint("- To import a published banner, click 'Remix Banner'.")
                .withHint(Component.literal("- JSST is not affiliated with PMC.").withStyle(Labels.WARNING))
                .build(), () -> {
            Sounds.interact(player);
            Menus.string(player, "", "Import PMC URL", CancellableCallback.of(code -> {
                var imported = BannerUtils.fromPMCCode(code);
                if (imported != null) {
                    Sounds.success(player);
                    stack = imported;
                    parseStack(false);
                } else {
                    Sounds.error(player);
                }
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));

        elements.put(37, new ItemGuiElement(Labels.create(BannerUtils.PMC_LINK_BANNER).withName("Open PMC Banner Editor").build(), () -> {
            player.closeContainer();
            var link = PMC_BANNER_URL + "?e=" + BannerUtils.toPMCCode(base, patterns);
            player.sendSystemMessage(CommandUtils.prefixed(CommandUtils.TextType.INFO, Component.literal(link).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)))));
        }));

        elements.put(38, Selector.create(ViewType.class, "Pattern View Type", this.viewType, newType -> {
            Sounds.interact(player);
            this.viewType = newType;
            open();
        }));

        elements.put(39, new ItemGuiElement(Labels.create(Items.GLOWSTONE_DUST).withName("Swap Colours").build(), () -> {
            Sounds.interact(player);
            Menus.colourSwap(player, ColourSwapMenu.BANNER.apply(build()), CancellableCallback.of(swap -> {
                Sounds.success(player);
                var translated = BannerUtils.colourSwap(new PatternDescription(base, patterns, itemType == ItemType.SHIELD), Map.of(swap.getFirst(), swap.getSecond()));
                this.base = translated.base;
                this.patterns = translated.patterns;
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));

        elements.put(45, Selector.create(ItemType.class, "Item Type", this.itemType, newType -> {
            Sounds.success(player);
            this.itemType = newType;
            open();
        }));

        elements.put(46, EditorUtils.clear(() -> {
            Sounds.clear(player);
            this.patterns.clear();
            open();
        }));

        elements.put(47, EditorUtils.reset(() -> {
            Sounds.clear(player);
            this.stack = getOriginal();
            parseStack(true);
            open();
        }));

        elements.put(48, EditorUtils.cancel(this::cancel));

        for (int i = 4; i < 54; i += 9) elements.put(i, EditorUtils.divider());
        for (int i = 14; i < 18; i++) elements.put(i, EditorUtils.divider());

        for (int i = 0; i < Math.min(this.patterns.size() + 1, MAX_PATTERNS); i++) {
            var slot = 23 + ((i / 4) * 9) + (i % 4);
            if (i < this.patterns.size()) {
                var pattern = this.patterns.get(i);
                var builder = this.viewType == ViewType.INDIVIDUAL ? BannerPatternMenu.PATTERN_ONLY : BannerPatternMenu.SANDWICH.build(itemType == ItemType.SHIELD, base, patterns.subList(0, i), Collections.emptyList());
                var label = Labels.create(builder.build(pattern))
                        .withName(BannerUtils.getName(pattern).copy().withStyle(CommandUtils.CLEAN))
                        .withHint("Click to change")
                        .build();
                int finalIndex = i;
                elements.put(slot, new ItemGuiElement(label, () -> {
                    Sounds.interact(player);
                    var before = patterns.subList(0, finalIndex);
                    List<Pair<Holder<BannerPattern>, DyeColor>> after = finalIndex != patterns.size() - 1 ? patterns.subList(finalIndex + 1, patterns.size()) : Collections.emptyList();
                    Menus.bannerPattern(player, BannerPatternMenu.SANDWICH.build(itemType == ItemType.SHIELD, base, before, after), pattern, CancellableCallback.of(newPattern -> {
                        if (newPattern.isPresent()) {
                            Sounds.success(player);
                            patterns.set(finalIndex, newPattern.get());
                            open();
                        } else {
                            Sounds.clear(player);
                            patterns.remove(finalIndex);
                            open();
                        }
                    }, () -> {
                        Sounds.error(player);
                        open();
                    }));
                }));
            } else { // add new
                elements.put(slot, new ItemGuiElement(Labels.create(Items.NETHER_STAR).withName("Add new pattern").build(), () -> {
                    Sounds.success(player);
                    patterns.add(Pair.of(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(BannerPatterns.CREEPER), DyeColor.GREEN));
                    open();
                }));
            }
        }

        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Banner"), elements));
    }

    public enum ItemType implements Selector.Labeled {
        BANNER(BannerUtils.JSST_SHIELD, "Banner"),
        SHIELD(BannerUtils.JSST_BANNER, "Shield");

        private final ItemStack label;
        private final String settingName;

        ItemType(ItemStack label, String settingName) {
            this.label = label;
            this.settingName = settingName;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }

    public enum ViewType implements Selector.Labeled {
        INDIVIDUAL(BannerUtils.builder(DyeColor.WHITE).add(BannerPatterns.CREEPER, DyeColor.RED).build(), "Individual Patterns"),
        CASCADING(BannerUtils.builder(DyeColor.WHITE).add(BannerPatterns.GRADIENT, DyeColor.BLUE).add(BannerPatterns.CURLY_BORDER, DyeColor.GREEN).add(BannerPatterns.CREEPER, DyeColor.RED).build(), "Cascading Patterns");

        private final ItemStack label;
        private final String settingName;

        ViewType(ItemStack label, String settingName) {
            this.label = label;
            this.settingName = settingName;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }

    public record PatternDescription(DyeColor base, List<Pair<Holder<BannerPattern>, DyeColor>> patterns, boolean isShield) {
        @Nullable
        public static PatternDescription from(ItemStack stack) {
            var item = stack.getItem();
            var patterns = BannerBlockEntity.createPatterns(DyeColor.BLACK, BannerBlockEntity.getItemPatterns(stack));
            patterns.remove(0);
            if (item instanceof BannerItem banner) {
                return new PatternDescription(banner.getColor(), patterns, false);
            } else if (item == Items.SHIELD) {
                var tag = BlockItem.getBlockEntityData(stack);
                var colour = DyeColor.WHITE;
                if (tag != null) {
                    colour = DyeColor.byId(tag.getInt(ShieldItem.TAG_BASE_COLOR));
                }
                return new PatternDescription(colour, patterns, true);
            }

            return null;
        }

        public ItemStack build() {
            return BannerUtils.builder(base).set(patterns).setShield(isShield).build();
        }
    }
}

package red.jackf.jsst.feature.itemeditor.gui.editors;

import com.mojang.datafixers.util.Pair;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.Formatting;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.feature.itemeditor.gui.menus.style.Colours;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.banners.Banners;
import red.jackf.jsst.util.sgui.banners.PMC;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.elements.SwitchButton;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.menus.selector.SelectorMenu;
import red.jackf.jsst.util.sgui.pagination.ListPaginator;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BannerEditor extends GuiEditor {
    public static final EditorType TYPE = new EditorType(
            JSST.id("banner"),
            BannerEditor::new,
            true,
            false,
            stack -> stack.is(Items.SHIELD) || stack.is(ItemTags.BANNERS),
            BannerEditor::getLabel
    );

    private static final Pattern PMC_CODE_GRAB = Pattern.compile("[a-zA-Z0-9]++$");
    private static final String PMC_URL_BASE = "https://www.planetminecraft.com/banner/?e=";
    private static final Map<DyeColor, Item> BY_COLOUR = new LinkedHashMap<>();

    static {
        BY_COLOUR.put(DyeColor.WHITE, Items.WHITE_BANNER);
        BY_COLOUR.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_BANNER);
        BY_COLOUR.put(DyeColor.GRAY, Items.GRAY_BANNER);
        BY_COLOUR.put(DyeColor.BLACK, Items.BLACK_BANNER);
        BY_COLOUR.put(DyeColor.BROWN, Items.BROWN_BANNER);
        BY_COLOUR.put(DyeColor.RED, Items.RED_BANNER);
        BY_COLOUR.put(DyeColor.ORANGE, Items.ORANGE_BANNER);
        BY_COLOUR.put(DyeColor.YELLOW, Items.YELLOW_BANNER);
        BY_COLOUR.put(DyeColor.LIME, Items.LIME_BANNER);
        BY_COLOUR.put(DyeColor.GREEN, Items.GREEN_BANNER);
        BY_COLOUR.put(DyeColor.CYAN, Items.CYAN_BANNER);
        BY_COLOUR.put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_BANNER);
        BY_COLOUR.put(DyeColor.BLUE, Items.BLUE_BANNER);
        BY_COLOUR.put(DyeColor.PURPLE, Items.PURPLE_BANNER);
        BY_COLOUR.put(DyeColor.MAGENTA, Items.MAGENTA_BANNER);
        BY_COLOUR.put(DyeColor.PINK, Items.PINK_BANNER);
    }

    private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns = new ArrayList<>(16);
    private ItemType itemType = ItemType.BANNER;
    private DyeColor baseColour = DyeColor.WHITE;

    public BannerEditor(
            ServerPlayer player,
            EditorContext context,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, context, initial, callback, false);
        this.setTitle(Component.translatable("jsst.itemEditor.banner"));

        this.loadItemTypeFromStack(this.stack);
        this.loadPatternsFromStack(this.stack);
        this.drawStatic();
    }

    private static GuiElementBuilderInterface<?> getLabel(EditorContext ctx) {
        return JSSTElementBuilder.ui(Banners.Misc.JSST)
                .setName(Component.translatable("jsst.itemEditor.banner"))
                .hideFlags();
    }

    private final ListPaginator<Pair<Holder<BannerPattern>, DyeColor>> paginator = ListPaginator.<Pair<Holder<BannerPattern>, DyeColor>>builder(this)
            .slots(4, 9, 0, 6)
            .list(this.patterns)
            .modifiable(this::getRandom, true)
            .max(16)
            .rowDraw(this::drawRow)
            .onUpdate(this::redraw)
            .build();

    private static DyeColor getBaseColour(ItemStack stack) {
        if (stack.getItem() instanceof BannerItem bannerItem) {
            return bannerItem.getColor();
        } else if (stack.is(Items.SHIELD)) {
            return ShieldItem.getColor(stack);
        } else {
            return DyeColor.WHITE;
        }
    }

    private List<GuiElementInterface> drawRow(int index, Pair<Holder<BannerPattern>, DyeColor> pair) {
        List<GuiElementInterface> icons = new ArrayList<>();
        ItemStack pattern = Banners.builder(pair.getSecond() == DyeColor.WHITE ? DyeColor.BLACK : DyeColor.WHITE)
                .add(pair.getFirst(), pair.getSecond())
                .build(false);
        icons.add(JSSTElementBuilder.ui(pattern)
                .setName(Banners.name(pair.getFirst(), pair.getSecond()))
                .leftClick(Component.translatable("jsst.itemEditor.banner.changePattern"), () -> {
                    Sounds.click(player);
                    Stream<Holder<BannerPattern>> patterns = this.context.server().registryAccess()
                            .registryOrThrow(Registries.BANNER_PATTERN)
                            .holders().map(ref -> ref); // still dont get types
                    SelectorMenu.<Holder<BannerPattern>>builder(player)
                            .labelMap(LabelMaps.BANNER_PATTERNS.apply(pair.getSecond()))
                            .options(patterns)
                            .createAndShow(result -> {
                                if (result.hasResult())
                                    this.patterns.set(index, Pair.of(result.result(), pair.getSecond()));
                                this.open();
                            });
                })
                .hideFlags()
                .build());
        icons.add(JSSTElementBuilder.ui(LabelMaps.DYES.getLabel(pair.getSecond()))
                .setName(Component.translatable("jsst.itemEditor.banner.colour", Translations.dye(pair.getSecond())))
                .leftClick(Component.translatable("jsst.itemEditor.banner.changeColour"), () -> {
                    Sounds.click(player);
                    SelectorMenu.open(player,
                            Component.translatable("jsst.itemEditor.banner.changeColour"),
                            List.of(DyeColor.values()),
                            LabelMaps.DYES,
                            result -> {
                                if (result.hasResult())
                                    this.patterns.set(index, Pair.of(pair.getFirst(), result.result()));
                                this.open();
                            });
                })
                .hideFlags()
                .build());
        return icons;
    }

    private Pair<Holder<BannerPattern>, DyeColor> getRandom() {
        var random = RandomSource.create();
        var pattern = this.context.server().registryAccess()
                .registryOrThrow(Registries.BANNER_PATTERN)
                .getRandom(random).orElseThrow();
        var colour = Colours.CANON_DYE_ORDER.get(random.nextInt(Colours.CANON_DYE_ORDER.size()));
        return Pair.of(pattern, colour);
    }

    @Override
    protected void onReset() {
        this.loadItemTypeFromStack(this.stack);
        this.loadPatternsFromStack(this.stack);
    }

    private void loadPatternsFromStack(ItemStack stack) {
        Banners.BannerPatterns loaded = Banners.parseStack(stack);

        this.baseColour = loaded.baseColour();
        this.patterns.clear();
        this.patterns.addAll(loaded.patterns());
    }

    private void loadItemTypeFromStack(ItemStack stack) {
        this.itemType = stack.is(Items.SHIELD) ? ItemType.SHIELD : ItemType.BANNER;
    }

    private void buildOutput() {
        ItemStack result = new ItemStack(this.itemType == ItemType.SHIELD ? Items.SHIELD : BY_COLOUR.get(this.baseColour));
        result.setTag(this.getInitial().getTag());

        var tagBuilder = new BannerPattern.Builder();
        this.patterns.forEach(tagBuilder::addPattern);
        var compound = new CompoundTag();
        compound.put(BannerBlockEntity.TAG_PATTERNS, tagBuilder.toListTag());
        if (this.itemType == ItemType.SHIELD) compound.putInt(ShieldItem.TAG_BASE_COLOR, this.baseColour.getId());
        BlockItem.setBlockEntityData(result, BlockEntityType.BANNER, compound);

        this.stack = result;
    }

    private void drawStatic() {
        Util.fill(this, CommonLabels.divider(), 3, 4, 0, 6);

        this.setSlot(Util.slot(0, 5), CommonLabels.cancel(this::cancel));

        if (JSST.CONFIG.instance().itemEditor.planetMinecraftButton)
            this.setSlot(Util.slot(0, 4), JSSTElementBuilder.ui(PMC.ICON)
                    .setName(PMC.NAME)
                    .leftClick(Component.translatable("jsst.itemEditor.banner.loadPMC.import"), () -> {
                        Sounds.click(player);
                        Menus.stringBuilder(player)
                                .title(Component.translatable("jsst.itemEditor.banner.loadPMC.import.title"))
                                .predicate(s -> s.strip().length() % 2 == 1)
                                .hint(JSSTElementBuilder.ui(PMC.ICON)
                                        .setName(Component.translatable("jsst.itemEditor.banner.loadPMC.import.hint1"))
                                        .addLoreLine(
                                                Component.literal(PMC_URL_BASE).append(Component.literal("6adei6cgbgbeb").withStyle(Styles.EXAMPLE)).withStyle(Styles.MINOR_LABEL)
                                        ).addLoreLine(Component.translatable("jsst.itemEditor.banner.loadPMC.import.hint2",
                                                Component.translatable("jsst.itemEditor.banner.loadPMC.import.hint2.button").withStyle(Styles.POSITIVE)))
                                        .asStack())
                                .createAndShow(result -> {
                                    if (result.hasResult()) {
                                        var matcher = PMC_CODE_GRAB.matcher(result.result().strip());
                                        if (matcher.find()) {
                                            String code = matcher.group();
                                            if (code.length() % 2 == 1) {
                                                this.loadPatternsFromStack(Banners.fromPMCCode(code));
                                            }
                                        }
                                    }
                                    this.open();
                                });
                    })
                    .rightClick(Component.translatable("jsst.itemEditor.banner.loadPMC.export"), () -> {
                        Sounds.click(player);
                        String url = PMC_URL_BASE + Banners.toPMCCode(this.baseColour, this.patterns);
                        Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                        this.player.sendSystemMessage(Formatting.successLine(Component.literal(url).withStyle(style)));
                    })
                    .addLoreLine(Component.translatable("jsst.itemEditor.banner.loadPMC.notAffiliated", PMC.NAME).withStyle(Styles.NEGATIVE)));

        this.setSlot(Util.slot(0, 3), JSSTElementBuilder.ui(Banners.fromPMCCode("dce2zby1317"))
                .hideFlags()
                .leftClick(Component.translatable("jsst.itemEditor.banner.colourChange"), () -> {
                    Sounds.click(player);
                    SelectorMenu.open(player,
                            Component.translatable("jsst.itemEditor.banner.colourChange.old"),
                            List.of(DyeColor.values()),
                            LabelMaps.DYES,
                            oldColour -> {
                                if (oldColour.hasResult()) {
                                    SelectorMenu.open(player,
                                            Component.translatable("jsst.itemEditor.banner.colourChange.new"),
                                            Arrays.stream(DyeColor.values()).filter(col -> col != oldColour.result()).toList(),
                                            LabelMaps.DYES,
                                            newColour -> {
                                                if (newColour.hasResult())
                                                    this.changeColour(oldColour.result(), newColour.result());
                                                this.open();
                                            });
                                } else {
                                    this.open();
                                }
                            });
                }));
    }

    private void changeColour(DyeColor oldCol, DyeColor newCol) {
        if (this.baseColour == oldCol)
            this.baseColour = newCol;

        List<Pair<Holder<BannerPattern>, DyeColor>> pairs = this.patterns;
        for (int i = 0; i < pairs.size(); i++) {
            Pair<Holder<BannerPattern>, DyeColor> pair = pairs.get(i);
            if (pair.getSecond() == oldCol) {
                patterns.set(i, Pair.of(pair.getFirst(), newCol));
            }
        }
    }

    @Override
    protected void redraw() {
        this.buildOutput();
        this.drawPreview(Util.slot(1, 1));

        this.setSlot(Util.slot(2, 4), JSSTElementBuilder.ui(LabelMaps.DYES.getLabel(this.baseColour))
                .setName(Component.translatable("jsst.itemEditor.banner.baseColour", Translations.dye(getBaseColour(this.stack))))
                .leftClick(Translations.change(), () -> {
                    Sounds.click(player);
                    SelectorMenu.open(player,
                            Component.translatable("jsst.itemEditor.banner.baseColour.change"),
                            List.of(DyeColor.values()),
                            LabelMaps.DYES,
                            result -> {
                                if (result.hasResult())
                                    this.baseColour = result.result();
                                this.open();
                            });
                }));

        this.paginator.draw();

        if (!this.context.cosmeticOnly()) {
            this.setSlot(Util.slot(1, 4), SwitchButton.<ItemType>builder(Component.translatable("jsst.itemEditor.banner.itemType"))
                    .addOption(ItemType.BANNER, JSSTElementBuilder.ui(Items.WHITE_BANNER)
                            .dontCleanText()
                            .setName(Component.translatable("jsst.itemEditor.banner.itemType.banner"))
                            .hideFlags()
                            .asStack())
                    .addOption(ItemType.SHIELD, JSSTElementBuilder.ui(Items.SHIELD)
                            .dontCleanText()
                            .setName(Component.translatable("jsst.itemEditor.banner.itemType.shield"))
                            .hideFlags()
                            .asStack())
                    .setCallback(type -> {
                        this.itemType = type;
                        this.redraw();
                    }).build(this.itemType));
        }
    }

    protected enum ItemType {
        BANNER,
        SHIELD
    }
}

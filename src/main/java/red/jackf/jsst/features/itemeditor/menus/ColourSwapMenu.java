package red.jackf.jsst.features.itemeditor.menus;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.editors.BannerEditor;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ColourSwapMenu {
    private final ServerPlayer player;
    private final PreviewBuilder builder;
    private final CancellableCallback<Pair<DyeColor, DyeColor>> callback;

    private DyeColor from = DyeColor.WHITE;
    private DyeColor to = DyeColor.WHITE;

    protected ColourSwapMenu(ServerPlayer player, PreviewBuilder builder, CancellableCallback<Pair<DyeColor, DyeColor>> callback) {
        this.player = player;
        this.builder = builder;
        this.callback = callback;
    }

    protected void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        elements.put(0, new ItemGuiElement(Labels.create(builder.build(Pair.of(from, to))).withHint("Click to finish").build(), () -> callback.accept(Pair.of(from, to))));
        elements.put(1, EditorUtils.divider());
        elements.put(2, new ItemGuiElement(Labels.create(DyeItem.byColor(from)).withName(ColourMenu.colourName(from.getName()).withStyle(CommandUtils.CLEAN.withColor(from.getTextColor()))).build(), () -> {
            Sounds.interact(player);
            var icons = Arrays.stream(Colour.DYE_CANON_ORDER)
                    .map(colour -> {
                        var stack = Labels.create(DyeItem.byColor(colour)).withName(ColourMenu.colourName(colour.getName()).copy().withStyle(CommandUtils.CLEAN.withColor(colour.getTextColor()))).build();
                        return Pair.of(colour, stack);
                    }).collect(EditorUtils.pairLinkedMapCollector());
            Menus.selector(player, icons, CancellableCallback.of(newFrom -> {
                Sounds.success(player);
                this.from = newFrom;
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        elements.put(3, new ItemGuiElement(Labels.create(DyeItem.byColor(to)).withName(ColourMenu.colourName(to.getName()).withStyle(CommandUtils.CLEAN.withColor(to.getTextColor()))).build(), () -> {
            Sounds.interact(player);
            var icons = Arrays.stream(Colour.DYE_CANON_ORDER)
                    .map(colour -> {
                        var stack = Labels.create(DyeItem.byColor(colour)).withName(ColourMenu.colourName(colour.getName()).copy().withStyle(CommandUtils.CLEAN.withColor(colour.getTextColor()))).build();
                        return Pair.of(colour, stack);
                    }).collect(EditorUtils.pairLinkedMapCollector());
            Menus.selector(player, icons, CancellableCallback.of(newTo -> {
                Sounds.success(player);
                this.to = newTo;
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        elements.put(4, EditorUtils.cancel(callback::cancel));

        player.openMenu(EditorUtils.make5x1(Component.literal("Swapping Colours"), elements));
    }

    public interface PreviewBuilder {
        ItemStack build(Pair<DyeColor, DyeColor> swap);
    }

    public static final Function<ItemStack, PreviewBuilder> BANNER = baseStack -> {
        var desc = BannerEditor.PatternDescription.from(baseStack);
        final var desc2 = desc == null ? new BannerEditor.PatternDescription(DyeColor.WHITE, new ArrayList<>(), false) : desc;
        return swap -> BannerUtils.colourSwap(desc2, Map.of(swap.getFirst(), swap.getSecond())).build();
    };
}

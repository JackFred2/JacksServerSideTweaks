package red.jackf.jsst.features.itemeditor.menus;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.function.TriFunction;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BannerPatternMenu {

    private final ServerPlayer player;
    private final PreviewBuilder previewBuilder;
    private Pair<Holder<BannerPattern>, DyeColor> pattern;
    private final CancellableCallback<Optional<Pair<Holder<BannerPattern>, DyeColor>>> callback;


    protected BannerPatternMenu(ServerPlayer player, PreviewBuilder previewBuilder, Pair<Holder<BannerPattern>, DyeColor> pattern, CancellableCallback<Optional<Pair<Holder<BannerPattern>, DyeColor>>> callback) {
        this.player = player;
        this.previewBuilder = previewBuilder;
        this.pattern = pattern;
        this.callback = callback;
    }
    
    protected void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(0, new ItemGuiElement(Labels.create(previewBuilder.build(pattern)).keepLore().withHint("Click to finish").build(), () -> callback.accept(Optional.of(pattern))));
        elements.put(1, EditorUtils.divider());
        elements.put(2, new ItemGuiElement(Labels.create(PATTERN_ONLY.build(pattern)).withName(BannerUtils.getName(pattern)).withHint("Click to change base pattern").build(), () -> {
            Sounds.interact(player);
            var icons = BannerUtils.getPatterns().stream()
                    .map(patternOption -> Pair.of(patternOption, previewBuilder.build(Pair.of(Holder.direct(patternOption), pattern.getSecond()))))
                    .collect(EditorUtils.pairLinkedMapCollector());
            Menus.selector(player, icons, CancellableCallback.of(newPattern -> {
                Sounds.success(player);
                pattern = Pair.of(Holder.direct(newPattern), pattern.getSecond());
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        elements.put(3, new ItemGuiElement(Labels.create(DyeItem.byColor(pattern.getSecond())).withName("Set Colour").build(), () -> {
            Sounds.interact(player);
            var icons = Arrays.stream(Colour.DYE_CANON_ORDER)
                    .map(colour -> {
                        var stack = Labels.create(previewBuilder.build(Pair.of(pattern.getFirst(), colour))).withName(ColourMenu.colourName(colour.getName()).copy().withStyle(Style.EMPTY.withItalic(false).withColor(colour.getTextColor()))).build();
                        return  Pair.of(colour, stack);
                    }).collect(EditorUtils.pairLinkedMapCollector());
            Menus.selector(player, icons, CancellableCallback.of(newColour -> {
                Sounds.success(player);
                pattern = Pair.of(pattern.getFirst(), newColour);
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        elements.put(7, new ItemGuiElement(Labels.create(Items.LAVA_BUCKET).withName("Delete Pattern").build(), () -> callback.accept(Optional.empty())));
        elements.put(8, EditorUtils.cancel(callback::cancel));
        
        player.openMenu(EditorUtils.make9x1(Component.nullToEmpty("Editing Banner Pattern"), elements));
    }

    public interface PreviewBuilder {
        ItemStack build(Pair<Holder<BannerPattern>, DyeColor> pattern);
    }

    public static final PreviewBuilder PATTERN_ONLY = pattern -> BannerUtils.builder(pattern.getSecond() == DyeColor.WHITE ? DyeColor.BLACK : DyeColor.WHITE).add(pattern).build();
    public static final TriFunction<DyeColor, List<Pair<Holder<BannerPattern>, DyeColor>>, List<Pair<Holder<BannerPattern>, DyeColor>>, PreviewBuilder> SANDWICH = (base, before, after) -> pattern -> {
        var builder = BannerUtils.builder(base);
        for (Pair<Holder<BannerPattern>, DyeColor> pair : before)
            builder.add(pair);
        builder.add(pattern);
        for (Pair<Holder<BannerPattern>, DyeColor> pair : after)
            builder.add(pair);
        return builder.build();
    };
}

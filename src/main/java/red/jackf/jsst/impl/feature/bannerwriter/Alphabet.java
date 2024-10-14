package red.jackf.jsst.impl.feature.bannerwriter;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
//? if >=1.21.1
import net.minecraft.core.component.DataComponents;
//? if <1.21.1 {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
*///?}
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
//? if >=1.21.1
import net.minecraft.world.level.block.entity.BannerPatternLayers;
//? if <1.21.1
/*import net.minecraft.world.level.block.entity.BlockEntityType;*/
import org.slf4j.Logger;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.utils.Banners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alphabet {
    private static final Logger LOGGER = JSST.getLogger("BannerWriter/Alphabet");
    public static final Alphabet INSTANCE = new Alphabet();

    private final Map<Character, CharDesign> characters = new HashMap<>();

    private Alphabet() {}

    public boolean containsDesignFor(char character) {
        return characters.containsKey(character);
    }

    public ItemStack create(char character, DyeColor backgroundColour, DyeColor textColour) {
        CharDesign design = characters.get(character);
        if (design == null) return ItemStack.EMPTY;

        ItemStack stack = Banners.ByColour.ITEM.get(design.flipBackground() ? textColour : backgroundColour).getDefaultInstance();

        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = design.patterns().stream()
                .map(layer -> Pair.of(layer.pattern(), layer.useBackgroundColour() ? backgroundColour : textColour))
                .toList();

        applyPatterns(stack, patterns);

        return stack;
    }

    //? if >=1.21.1 {
    private void applyPatterns(ItemStack stack, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        BannerPatternLayers.Builder builder = new BannerPatternLayers.Builder();

        for (Pair<Holder<BannerPattern>, DyeColor> pattern : patterns) {
            builder.add(pattern.getFirst(), pattern.getSecond());
        }

        stack.set(DataComponents.BANNER_PATTERNS, builder.build());
    }
    //?} else {
    /*private void applyPatterns(ItemStack stack, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        CompoundTag tag = new CompoundTag();

        var builder = new BannerPattern.Builder();
        patterns.forEach(builder::addPattern);
        tag.put("Patterns", builder.toListTag());

        BlockItem.setBlockEntityData(stack, BlockEntityType.BANNER, tag);
    }
    *///?}

    private void tryLoad(RegistryAccess.Frozen registries, char character, String PMCcode) {
        DataResult<Pair<DyeColor, List<Pair<Holder<BannerPattern>, DyeColor>>>> parsed = Banners.PMC.parsePMCCode(registries, PMCcode);
        if (parsed.error().isPresent()) {
            LOGGER.error("Couldn't parse PMC code '{}': {}", PMCcode, parsed.error().get().message());
            return;
        }

        //noinspection OptionalGetWithoutIsPresent
        Pair<DyeColor, List<Pair<Holder<BannerPattern>, DyeColor>>> result = parsed.result().get();

        boolean flipBackground = result.getFirst() != DyeColor.BLACK;

        List<CharDesignLayer> layers = result.getSecond().stream()
                        .map(pair -> new CharDesignLayer(pair.getSecond() == DyeColor.BLACK, pair.getFirst()))
                        .toList();

        characters.put(character, new CharDesign(flipBackground, layers));
    }

    public void reload(RegistryAccess.Frozen registryAccess) {
        characters.clear();

        tryLoad(registryAccess, 'a', "1gugsgtgv13"); // Credits: https://www.gamergeeks.net/apps/minecraft/banners/letters
        tryLoad(registryAccess, 'b', "1gugogv18gsgt13");
        tryLoad(registryAccess, 'c', "1gvgogu1tgs13");
        tryLoad(registryAccess, 'd', "1gugogv18gs13");
        tryLoad(registryAccess, 'e', "1gsgvgtgo13");
        tryLoad(registryAccess, 'f', "1gt1ugvgs13");
        tryLoad(registryAccess, 'g', "1gu1dgogsgv13");
        tryLoad(registryAccess, 'h', "g1v1ogsgu13");
        tryLoad(registryAccess, 'i', "1gpgvgo13");
        tryLoad(registryAccess, 'j', "1gs1dgogu13");
        tryLoad(registryAccess, 'k', "1gr1dgqgs13");
        tryLoad(registryAccess, 'l', "1gogs13");
        tryLoad(registryAccess, 'm', "1gz1xgsgu13");
        tryLoad(registryAccess, 'n', "1gs1zgrgu13");
        tryLoad(registryAccess, 'o', "1gsgugogv13");
        tryLoad(registryAccess, 'p', "1gu1Dgtgvgs13");
        tryLoad(registryAccess, 'q', "g1ggugsgk13");
        tryLoad(registryAccess, 'r', "1gd1pgvgsgr13");
        tryLoad(registryAccess, 's', "g1g1tgr13");
        tryLoad(registryAccess, 't', "1gvgp13");
        tryLoad(registryAccess, 'u', "1gogsgu13");
        tryLoad(registryAccess, 'v', "1gqgs1ygq13");
        tryLoad(registryAccess, 'w', "1gy1wgsgu13");
        tryLoad(registryAccess, 'x', "1g713");
        tryLoad(registryAccess, 'y', "1gr1Dgq13");
        tryLoad(registryAccess, 'z', "1gvgqgo13");
        tryLoad(registryAccess, '0', "1gogsgvgugq13"); // Credits: https://www.gamergeeks.net/apps/minecraft/banners/numbers
        tryLoad(registryAccess, '1', "1gpgl18go13");
        tryLoad(registryAccess, '2', "1gv1ggogq13");
        tryLoad(registryAccess, '3', "1gogtgv18gu13");
        tryLoad(registryAccess, '4', "1gs1Dgugt13");
        tryLoad(registryAccess, '5', "1go1ggvgr13");
        tryLoad(registryAccess, '6', "1gogu1dgtgvgs13");
        tryLoad(registryAccess, '7', "1gqgv13");
        tryLoad(registryAccess, '8', "1gvgsgtgogu13");
        tryLoad(registryAccess, '9', "1gs1Dgtgvgugo13");
        tryLoad(registryAccess, ' ', "1"); // custom from this point
        tryLoad(registryAccess, '.', "g1u1s1d161315");
        tryLoad(registryAccess, ',', "g1u1s1d1319");
        tryLoad(registryAccess, '+', "1gn1v1o13");
        tryLoad(registryAccess, '-', "1gt13");
        tryLoad(registryAccess, '*', "1gh1d13");
        tryLoad(registryAccess, '/', "1gq1318");
        tryLoad(registryAccess, '\'', "1gp1D1h1315");
        tryLoad(registryAccess, '"', "g131D1p161t");
        tryLoad(registryAccess, '=', "g131v1o1t");
        tryLoad(registryAccess, '!', "g131s1u1t1o");
        tryLoad(registryAccess, ':', "g1t1s1u1o1v");
    }

    public record CharDesignLayer(boolean useBackgroundColour, Holder<BannerPattern> pattern) {}

    // background black text white
    public record CharDesign(boolean flipBackground, List<CharDesignLayer> patterns) {}
}

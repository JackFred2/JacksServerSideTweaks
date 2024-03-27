package red.jackf.jsst.feature.bannerwriter;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.sgui.banners.Banners;
import red.jackf.jsst.util.sgui.banners.PMC;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Characters {
    public static final Characters INSTANCE = new Characters();
    private Characters() {}

    // White Text, Black Background
    // Lower Case
    private final Map<Character, String> CHARACTER_BANNER_CODES = new HashMap<>();

    public Set<Character> validCharacters() {
        return ImmutableSet.copyOf(CHARACTER_BANNER_CODES.keySet());
    }

    public Optional<ItemStack> getLetter(char character, DyeColor textColour, DyeColor backgroundColour) {
        character = Character.toLowerCase(character);
        String rawPmcCode = CHARACTER_BANNER_CODES.get(character);
        if (rawPmcCode == null) return Optional.empty();

        StringBuilder changedCode = new StringBuilder();
        char[] characters = rawPmcCode.toCharArray();
        final char textFrom = PMC.COLOURS.inverse().get(DyeColor.WHITE);
        final char textTo = PMC.COLOURS.inverse().get(textColour);
        final char backgroundFrom = PMC.COLOURS.inverse().get(DyeColor.BLACK);
        final char backgroundTo = PMC.COLOURS.inverse().get(backgroundColour);
        for (int i = 0; i < characters.length; i++) {
            if (i % 2 == 0 && i != 0) {
                changedCode.append(characters[i]);
            } else {
                if (characters[i] == textFrom) {
                    changedCode.append(textTo);
                } else if (characters[i] == backgroundFrom) {
                    changedCode.append(backgroundTo);
                } else {
                    changedCode.append(characters[i]);
                }
            }
        }

        return Optional.ofNullable(Banners.fromPMCCode(changedCode.toString()));
    }

    public Processed process(String text) {
        text = text.toLowerCase();
        Set<Character> allowed = validCharacters();
        StringBuilder valid = new StringBuilder();
        StringBuilder invalid = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (!allowed.contains(c)) {
                invalid.append(c);
            } else {
                valid.append(c);
            }
        }
        return new Processed(
                valid.toString(),
                Optional.ofNullable(invalid.isEmpty() ? null : invalid.toString())
        );
    }

    public void setup() {
        CHARACTER_BANNER_CODES.clear();

        CHARACTER_BANNER_CODES.put('a', "1gugsgtgv13"); // Credits: https://www.gamergeeks.net/apps/minecraft/banners/letters
        CHARACTER_BANNER_CODES.put('b', "1gugogv18gsgt13");
        CHARACTER_BANNER_CODES.put('c', "1gvgogu1tgs13");
        CHARACTER_BANNER_CODES.put('d', "1gugogv18gs13");
        CHARACTER_BANNER_CODES.put('e', "1gsgvgtgo13");
        CHARACTER_BANNER_CODES.put('f', "1gt1ugvgs13");
        CHARACTER_BANNER_CODES.put('g', "1gu1dgogsgv13");
        CHARACTER_BANNER_CODES.put('h', "g1v1ogsgu13");
        CHARACTER_BANNER_CODES.put('i', "1gpgvgo13");
        CHARACTER_BANNER_CODES.put('j', "1gs1dgogu13");
        CHARACTER_BANNER_CODES.put('k', "1gr1dgqgs13");
        CHARACTER_BANNER_CODES.put('l', "1gogs13");
        CHARACTER_BANNER_CODES.put('m', "1gz1xgsgu13");
        CHARACTER_BANNER_CODES.put('n', "1gs1zgrgu13");
        CHARACTER_BANNER_CODES.put('o', "1gsgugogv13");
        CHARACTER_BANNER_CODES.put('p', "1gu1Dgtgvgs13");
        CHARACTER_BANNER_CODES.put('q', "g1ggugsgk13");
        CHARACTER_BANNER_CODES.put('r', "1gd1pgvgsgr13");
        CHARACTER_BANNER_CODES.put('s', "g1g1tgr13");
        CHARACTER_BANNER_CODES.put('t', "1gvgp13");
        CHARACTER_BANNER_CODES.put('u', "1gogsgu13");
        CHARACTER_BANNER_CODES.put('v', "1gqgs1ygq13");
        CHARACTER_BANNER_CODES.put('w', "1gy1wgsgu13");
        CHARACTER_BANNER_CODES.put('x', "1g713");
        CHARACTER_BANNER_CODES.put('y', "1gr1Dgq13");
        CHARACTER_BANNER_CODES.put('z', "1gvgqgo13");
        CHARACTER_BANNER_CODES.put('0', "1gogsgvgugq13"); // Credits: https://www.gamergeeks.net/apps/minecraft/banners/numbers
        CHARACTER_BANNER_CODES.put('1', "1gpgl18go13");
        CHARACTER_BANNER_CODES.put('2', "1gv1ggogq13");
        CHARACTER_BANNER_CODES.put('3', "1gogtgv18gu13");
        CHARACTER_BANNER_CODES.put('4', "1gs1Dgugt13");
        CHARACTER_BANNER_CODES.put('5', "1go1ggvgr13");
        CHARACTER_BANNER_CODES.put('6', "1gogu1dgtgvgs13");
        CHARACTER_BANNER_CODES.put('7', "1gqgv13");
        CHARACTER_BANNER_CODES.put('8', "1gvgsgtgogu13");
        CHARACTER_BANNER_CODES.put('9', "1gs1Dgtgvgugo13");
        CHARACTER_BANNER_CODES.put(' ', "1"); // custom from this point
        CHARACTER_BANNER_CODES.put('.', "g1u1s1d161315");
        CHARACTER_BANNER_CODES.put(',', "g1u1s1d1319");
        CHARACTER_BANNER_CODES.put('+', "1gn1v1o13");
        CHARACTER_BANNER_CODES.put('-', "1gt13");
        CHARACTER_BANNER_CODES.put('*', "1gh1d13");
        CHARACTER_BANNER_CODES.put('/', "1gq1318");
        CHARACTER_BANNER_CODES.put('\'', "1gp1D1h1315");
        CHARACTER_BANNER_CODES.put('"', "g131D1p161t");
        CHARACTER_BANNER_CODES.put('=', "g131v1o1t");
        CHARACTER_BANNER_CODES.put('!', "g131s1u1t1o");
        CHARACTER_BANNER_CODES.put(':', "g1t1s1u1o1v");
    }

    public record Processed(String text, Optional<String> invalid) {
    }
}

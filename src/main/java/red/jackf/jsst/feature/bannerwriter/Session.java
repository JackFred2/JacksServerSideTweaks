package red.jackf.jsst.feature.bannerwriter;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class Session {
    private final DyeColor backgroundColour;
    private final DyeColor textColour;
    private final Deque<Character> text;

    public Session(DyeColor backgroundColour, DyeColor textColour, String text) {
        this.backgroundColour = backgroundColour;
        this.textColour = textColour;
        this.text = new ArrayDeque<>();

        for (char c : text.toCharArray()) {
            if (Characters.INSTANCE.validCharacters().contains(c)) {
                this.text.addLast(c);
            }
        }
    }

    public DyeColor getBackgroundColour() {
        return backgroundColour;
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }

    public boolean stillValid(ServerPlayer player) {
        var background = BannerWriter.getBaseColourFromBlankBanner(player.getItemInHand(InteractionHand.MAIN_HAND))
                .or(() -> BannerWriter.getBaseColourFromBlankBanner(player.getItemInHand(InteractionHand.OFF_HAND)));
        if (background.isEmpty() || background.get() != this.backgroundColour) return false;

        return !player.isRemoved();
    }

    public boolean isCorrectBanner(ItemStack stack) {
        return BannerWriter.getBaseColourFromBlankBanner(stack).map(col -> col == this.backgroundColour).orElse(false);
    }

    public Optional<ItemStack> next() {
        if (isEmpty()) return Optional.empty();

        return Characters.INSTANCE.getLetter(this.text.pop(), this.textColour, this.backgroundColour);
    }
}

package red.jackf.jsst.features.itemeditor.menus;

import com.mojang.datafixers.util.Pair;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;

public class Menus {
    private Menus() {}

    public static void string(ServerPlayer player, String baseText, CancellableCallback<String> callback) {
        string(player, baseText, "Editing text", callback);
    }

    public static void string(ServerPlayer player, String baseText, String title, CancellableCallback<String> callback) {
        player.openMenu(new SimpleMenuProvider(((i, inventory, player1) -> {
            var menu = new AnvilMenu(i, inventory);
            var elements = new HashMap<Integer, ItemGuiElement>();
            elements.put(AnvilMenu.INPUT_SLOT, new ItemGuiElement(Labels.create(Items.PAPER).withName(baseText).withHint("Click input to cancel").withHint("Click output to confirm").build(), callback::cancel));
            elements.put(AnvilMenu.RESULT_SLOT, new ItemGuiElement(Labels.blank(), () -> {
                var item = menu.slots.get(AnvilMenu.RESULT_SLOT).getItem();
                if (item.isEmpty())
                    callback.cancel();
                else
                    callback.accept(item.hasCustomHoverName() ? item.getHoverName().getString() : "");
            }));
            elements.forEach((slot, label) -> menu.slots.get(slot).set(label.label()));
            //noinspection DataFlowIssue
            ((JSSTSealableMenuWithButtons) menu).jsst_sealWithButtons(elements);
            return menu;
        }), literal(title)));
    }

    // Returns a duration in ticks, from -1 to positive infinity
    public static void duration(ServerPlayer player, int ticks, CancellableCallback<Integer> callback) {
        string(player, ticks % SharedConstants.TICKS_PER_SECOND == 0 ? String.valueOf(ticks / 20) : "%dt".formatted(ticks), "Editing Duration", CancellableCallback.of(s -> {
            if ("-1".equals(s) || "-1t".equals(s) || "inf".equals(s) || "infinite".equals(s)) {
                callback.accept(-1);
                return;
            }
            try {
                if (s.charAt(s.length() - 1) == 't') {
                    callback.accept(Integer.parseUnsignedInt(s.substring(0, s.length() - 1)));
                } else {
                    callback.accept(Integer.parseUnsignedInt(s) * SharedConstants.TICKS_PER_SECOND);
                }
            } catch (NumberFormatException ex) {
                callback.cancel();
            }
        }, callback::cancel));
    }

    // Returns a double that is non-infinite and non-NaN
    public static void decimal(ServerPlayer player, double value, CancellableCallback<Double> callback) {
        string(player, "%.2f".formatted(value), CancellableCallback.of(d -> {
            try {
                var newValue = Double.valueOf(d);
                if (!newValue.isInfinite() && !newValue.isNaN()) {
                    callback.accept(newValue);
                } else {
                    callback.cancel();
                }
            } catch (NumberFormatException ex) {
                callback.cancel();
            }
        }, callback::cancel));
    }

    public static void style(ServerPlayer player, Component preview, Consumer<Component> callback) {
        var style = new StyleMenu(player, preview, callback);
        style.open();
    }

    public static void component(ServerPlayer player, Function<Component, ItemStack> previewBuilder, @Nullable Component original, int maxComponents, CancellableCallback<Component> callback) {
        var componentMenu = new AdvancedComponentMenu(player, previewBuilder, original, maxComponents, callback);
        componentMenu.open();
    }

    public static <T> void selector(ServerPlayer player, Map<T, ItemStack> options, CancellableCallback<T> callback) {
        var selector = new SelectorMenu<>(player, options, callback);
        selector.open();
    }

    public static void loreVisibility(ServerPlayer player, ItemStack stack, CancellableCallback<ItemStack> callback) {
        var loreVisiblity = new LoreVisiblityMenu(player, stack, callback);
        loreVisiblity.open();
    }

    public static void colour(ServerPlayer player, CancellableCallback<Colour> callback) {
        var colour = new ColourMenu(player, CancellableCallback.of(opt -> {
            if (opt.isPresent()) callback.accept(opt.get());
            else callback.cancel();
        }, callback::cancel), false);
        colour.open();
    }

    public static void removeableColour(ServerPlayer player, CancellableCallback<Optional<Colour>> callback) {
        var colour = new ColourMenu(player, callback, true);
        colour.open();
    }

    public static void colourSwap(ServerPlayer player, ColourSwapMenu.PreviewBuilder builder, CancellableCallback<Pair<DyeColor, DyeColor>> callback) {
        var colourSwap = new ColourSwapMenu(player, builder, callback);
        colourSwap.open();
    }

    public static void gradient(ServerPlayer player, Gradient start, CancellableCallback<Gradient> callback) {
        var gradient = new GradientMenu(player, start, callback);
        gradient.open();
    }

    public static void mobEffect(ServerPlayer player, CancellableCallback<MobEffect> callback) {
        selector(player, LabelData.MOB_EFFECTS.getIconList(BuiltInRegistries.MOB_EFFECT), callback);
    }

    public static void bannerPattern(ServerPlayer player, BannerPatternMenu.PreviewBuilder previewBuilder, Pair<Holder<BannerPattern>, DyeColor> pattern, CancellableCallback<Optional<Pair<Holder<BannerPattern>, DyeColor>>> callback) {
        var bannerPattern = new BannerPatternMenu(player, previewBuilder, pattern, callback);
        bannerPattern.open();
    }

    public static void attribute(ServerPlayer player, CancellableCallback<Attribute> callback) {
        selector(player, LabelData.ATTRIBUTES.getIconList(BuiltInRegistries.ATTRIBUTE), callback);
    }

    public static void enchantment(ServerPlayer player, CancellableCallback<Enchantment> callback) {
        selector(player, LabelData.ENCHANTMENTS.getIconList(BuiltInRegistries.ENCHANTMENT), callback);
    }
}

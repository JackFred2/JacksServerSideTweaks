package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;

public class Menus {
    private Menus() {}

    public static void string(ServerPlayer player, String text, Consumer<String> callback) {
        string(player, text, "Editing text", callback);
    }

    public static void string(ServerPlayer player, String text, String title, Consumer<String> callback) {
        player.openMenu(new SimpleMenuProvider(((i, inventory, player1) -> {
            var menu = new AnvilMenu(i, inventory);
            var elements = new HashMap<Integer, ItemGuiElement>();
            elements.put(AnvilMenu.INPUT_SLOT, new ItemGuiElement(Labels.create(Items.PAPER).withName(text).withHint("Click to confirm").build(), () -> callback.accept(text)));
            elements.put(AnvilMenu.RESULT_SLOT, new ItemGuiElement(Labels.blank(), () -> {
                var item = menu.slots.get(AnvilMenu.RESULT_SLOT).getItem();
                if (item.isEmpty() || !item.hasCustomHoverName())
                    callback.accept(text);
                else
                    callback.accept(item.getHoverName().getString());
            }));
            elements.forEach((slot, label) -> menu.slots.get(slot).set(label.label()));
            //noinspection DataFlowIssue
            ((JSSTSealableMenuWithButtons) menu).jsst_sealWithButtons(elements);
            return menu;
        }), literal(title)));
    }

    // Returns a duration in ticks, from -1 to positive infinity
    public static void duration(ServerPlayer player, int ticks, CancellableCallback<Integer> callback) {
        string(player, ticks % SharedConstants.TICKS_PER_SECOND == 0 ? String.valueOf(ticks / 20) : "%dt".formatted(ticks), "Editing Duration", s -> {
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
        });
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
        var colour = new ColourMenu(player, callback);
        colour.open();
    }

    public static void gradient(ServerPlayer player, Gradient start, CancellableCallback<Gradient> callback) {
        var gradient = new GradientMenu(player, start, callback);
        gradient.open();
    }

    public static void mobEffect(ServerPlayer player, CancellableCallback<MobEffect> callback) {
        var mobEffect = new MobEffectMenu(player, callback);
        mobEffect.open();
    }
}

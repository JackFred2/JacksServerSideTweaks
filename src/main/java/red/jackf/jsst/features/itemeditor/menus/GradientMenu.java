package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;

public class GradientMenu {
    private final ServerPlayer player;
    private Gradient gradient;
    private final CancellableCallback<Gradient> callback;

    protected GradientMenu(ServerPlayer player, Gradient gradient, CancellableCallback<Gradient> callback) {
        this.player = player;
        this.gradient = gradient;
        this.callback = callback;
    }

    protected void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        elements.put(0, new ItemGuiElement(EditorUtils.withHint(gradient.start().label(), "Click to change"), () -> Menus.colour(player, CancellableCallback.of(colour -> {
            Sounds.success(player);
            this.gradient = new Gradient(colour, this.gradient.end(), this.gradient.mode());
            open();
        }, () -> {
            Sounds.error(player);
            open();
        }))));

        var applicator = new StyleMenu.GradientColour(gradient);
        elements.put(2, new ItemGuiElement(Labels.create(Items.GLOWSTONE_DUST).withName(applicator.set(Component.literal("|".repeat(50)), Labels.CLEAN)).withHint("Click to finish").build(), () -> callback.accept(this.gradient)));

        elements.put(4, new ItemGuiElement(EditorUtils.withHint(gradient.end().label(), "Click to change"), () -> Menus.colour(player, CancellableCallback.of(colour -> {
            Sounds.success(player);
            this.gradient = new Gradient(this.gradient.start(), colour, this.gradient.mode());
            open();
        }, () -> {
            Sounds.error(player);
            open();
        }))));

        elements.put(5, EditorUtils.divider());
        elements.put(6, new ItemGuiElement(Labels.create(Items.BLAZE_POWDER).withName("Flip Colours").build(), () -> {
            Sounds.write(player);
            this.gradient = new Gradient(this.gradient.end(), this.gradient.start(), this.gradient.mode());
            open();
        }));
        elements.put(7, new ItemGuiElement(this.gradient.mode().label(), () -> {
            Sounds.interact(player);
            var nextMode = Gradient.Mode.values()[(this.gradient.mode().ordinal() + 1) % Gradient.Mode.values().length];
            this.gradient = new Gradient(this.gradient.start(), this.gradient.end(), nextMode);
            open();
        }));
        elements.put(8, EditorUtils.cancel(callback::cancel));

        player.openMenu(EditorUtils.make9x1(Component.literal("Edit Gradient"), elements));
    }

}

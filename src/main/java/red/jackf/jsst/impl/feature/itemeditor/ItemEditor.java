package red.jackf.jsst.impl.feature.itemeditor;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.config.JSSTConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemEditor {
    private static final Map<ServerPlayer, EditSession> CURRENT_SESSIONS = new HashMap<>();

    public static void setup() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = Commands.literal("itemEditor")
                    .requires(stack -> stack.isPlayer() && stack.hasPermission(4))
                    .executes(ItemEditor::onCommand);

            dispatcher.register(root);
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (Iterator<EditSession> iterator = CURRENT_SESSIONS.values().iterator(); iterator.hasNext(); ) {
                EditSession value = iterator.next();
                if (!value.stillValid()) {
                    iterator.remove();
                    value.cancel();
                }
            }
        });
    }

    private static int onCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        JSSTConfig.ItemEditor config = JSSTConfig.INSTANCE.instance().itemEditor;

        if (!config.enabled) {
            ctx.getSource().sendSystemMessage(Component.translatable("jsst.feature.disabled"));
            return 0;
        }

        ServerPlayer player = ctx.getSource().getPlayerOrException();

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHand.isEmpty()) {
            start(player, mainHand, () -> player.getItemInHand(InteractionHand.MAIN_HAND) == mainHand, stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack));
            return 1;
        }

        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!mainHand.isEmpty()) {
            start(player, offHand, () -> player.getItemInHand(InteractionHand.OFF_HAND) == offHand, stack -> player.setItemInHand(InteractionHand.OFF_HAND, stack));
            return 2;
        }

        // TODO check non-cosmetic and offer item choice

        return 0;
    }

    private static void start(ServerPlayer player, ItemStack stack, Supplier<Boolean> stillValid, Consumer<ItemStack> onComplete) {
        EditSession session = new EditSession(player, stack, stillValid, onComplete);

        CURRENT_SESSIONS.put(player, session);

        new MainGui(session).open();
    }
}

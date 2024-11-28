package red.jackf.jsst.impl.feature.itemeditor;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.feature.itemeditor.gui.MainGui;
import red.jackf.jsst.impl.feature.itemeditor.gui.editors.*;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.sgui.menus.selection.SelectionMenu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ItemEditor {
    public static final Logger LOGGER = JSST.getLogger("Item Editor");

    public static final List<Editor.Type<?>> EDITORS = List.of(
            SimpleNameEditor.TYPE,
            EnchantmentEditor.TYPE,
            ArmourTrimEditor.TYPE,
            MapColourEditor.TYPE,
            DyeColourEditor.TYPE,
            DecoratedPotEditor.TYPE,
            BannerEditor.TYPE,
            DurabiltyEditor.TYPE,
            StackSizeEditor.TYPE,
            GlintEditor.TYPE,
            PlayerHeadNameEditor.TYPE,
            LabelMapExport.TYPE
    );

    private static final Map<ServerPlayer, EditSession> CURRENT_SESSIONS = new HashMap<>();

    public static void setup() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = Commands.literal("itemEditor")
                    .requires(stack -> stack.isPlayer() && (JSSTConfig.INSTANCE.instance().itemEditor.nonOpsCanUseCosmeticMode || stack.hasPermission(4)))
                    .executes(ItemEditor::onCommand);

            dispatcher.register(root);
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (Iterator<EditSession> iterator = CURRENT_SESSIONS.values().iterator(); iterator.hasNext(); ) {
                EditSession value = iterator.next();
                if (!value.stillValid()) {
                    iterator.remove();
                    value.end();
                }
            }
        });
    }

    private static Access getAccessFor(ServerPlayer player) {
        JSSTConfig.ItemEditor config = JSSTConfig.INSTANCE.instance().itemEditor;

        if (!config.enabled) return Access.NONE;

        boolean isOp = player.hasPermissions(4);

        if (isOp) {
            return Access.FULL;
        } else if (config.nonOpsCanUseCosmeticMode) {
            return Access.COSMETIC;
        } else {
            return Access.NONE;
        }
    }

    private static Stream<Item> getItemsStream(RegistryAccess access) {
        return RegistryUtils.stream(RegistryUtils.lookup(access, Registries.ITEM))
                .map(Holder::value)
                .filter(item -> item != Items.AIR);
    }

    private static int onCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        JSSTConfig.ItemEditor config = JSSTConfig.INSTANCE.instance().itemEditor;

        if (!config.enabled) {
            ctx.getSource().sendSystemMessage(Component.translatable("jsst.feature.disabled"));
            return 0;
        }

        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Access access = getAccessFor(player);

        if (access == Access.NONE) {
            ctx.getSource().sendSystemMessage(Component.translatable("jsst.itemEditor.noPermissions"));
            return 0;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHand.isEmpty()) {
            start(player, mainHand, access, () -> player.getItemInHand(InteractionHand.MAIN_HAND) == mainHand, stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack));
            return 1;
        }

        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!mainHand.isEmpty()) {
            start(player, offHand, access, () -> player.getItemInHand(InteractionHand.OFF_HAND) == offHand, stack -> player.setItemInHand(InteractionHand.OFF_HAND, stack));
            return 2;
        }

        if (access == Access.FULL) {
            SelectionMenu.<Item>builder(player)
                    .title(Component.translatable("jsst.itemEditor.selectItem"))
                    .labelStacks(Item::getDefaultInstance)
                    .options(getItemsStream(player.registryAccess()))
                    .filterable((item, text) -> RegistryUtils.lookup(player.registryAccess(), Registries.ITEM)
                            .getResourceKey(item)
                            .map(key -> key.location().toString().contains(text.toLowerCase()))
                            .orElse(false))
                    .start(item -> {
                        if (item.isPresent()) {
                            start(player, item.get().getDefaultInstance(), access, () -> true, stack -> player.getInventory().add(stack));
                        } else {
                            player.closeContainer();
                        }
                    });
        } else {
            ctx.getSource().sendSystemMessage(Component.translatable("jsst.itemEditor.noItem"));
        }

        return 0;
    }

    private static void start(ServerPlayer player, ItemStack initial, Access access, Supplier<Boolean> stillValid, Consumer<ItemStack> onComplete) {
        EditSession session = new EditSession(player, initial, access != Access.FULL, stillValid);

        CURRENT_SESSIONS.put(player, session);

        MainGui gui = new MainGui(session, result -> {
            if (result.hasResult()) {
                onComplete.accept(result.result());
            }
            session.end();
        });

        gui.open();
    }
}

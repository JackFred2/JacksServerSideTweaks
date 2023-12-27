package red.jackf.jsst.feature.itemeditor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;

public class ItemEditorCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandBuildContext buildCtx) {
        var root = Commands.literal("itemEditor")
                           .requires(CommandSourceStack::isPlayer)
                           .executes(ctx -> onHand(buildCtx, ctx));

        var itemId = Commands.argument("itemId", ItemArgument.item(buildCtx))
                             .executes(ItemEditorCommand::onItemId);

        root.then(itemId);

        return root;
    }

    private static int onItemId(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ItemInput item = ItemArgument.getItem(ctx, "itemId");
        ItemEditor.INSTANCE.newSession(ctx.getSource().getPlayerOrException(), item.createItemStack(1, false), null);
        return 1;
    }

    private static int onHand(
            CommandBuildContext buildCtx,
            CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();


        ItemStack handItem = player.getMainHandItem();
        EquipmentSlot slot = EquipmentSlot.MAINHAND;
        if (handItem.isEmpty()) {
            handItem = player.getOffhandItem();
            slot = EquipmentSlot.OFFHAND;
        }

        if (handItem.isEmpty()) {
            Menus.selector(player,
                           Component.translatable("jsst.itemEditor.selectBaseItem"),
                           buildCtx.holderLookup(Registries.ITEM).listElements().map(Holder.Reference::value).filter(item -> !(item == Items.AIR)).toList(),
                           LabelMaps.ITEMS,
                           selection -> {
                               if (selection.hasResult()) {
                                   ItemEditor.INSTANCE.newSession(player, new ItemStack(selection.result()), null);
                               } else {
                                   player.closeContainer();
                               }
                           });
        } else {
            ItemEditor.INSTANCE.newSession(player, handItem, slot);
        }
        return 1;
    }
}
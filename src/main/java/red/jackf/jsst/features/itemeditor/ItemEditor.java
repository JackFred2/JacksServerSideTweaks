package red.jackf.jsst.features.itemeditor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

import static net.minecraft.commands.Commands.literal;

public class ItemEditor extends Feature<ItemEditor.Config> {
    private static Component failure(String text) {
        return Component.literal("[x] ").withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal(text).withStyle(ChatFormatting.WHITE));
    }

    private static void startEditor(CommandSourceStack source, ItemStack item) throws CommandSyntaxException {
        new EditSession(source, item).start();
    }

    @Override
    public void init() {

    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(literal("editName").executes(ctx -> {

            return 0;
        })).then(literal("editLore").executes(ctx -> {

            return 0;
        })).then(literal("editEnchantments").executes(ctx -> {

            return 0;
        })).then(literal("editPotionEffects").executes(ctx -> {

            return 0;
        })).executes(ctx -> {
            if (!getConfig().enabled) {
                ctx.getSource().sendFailure(failure("feature disabled!"));
                return 0;
            }
            var player = ctx.getSource().getPlayerOrException();
            var item = player.getMainHandItem();
            if (item.isEmpty()) item = player.getOffhandItem();
            if (item.isEmpty()) {
                ctx.getSource().sendFailure(failure("no item in hand!"));
                return 0;
            }
            startEditor(ctx.getSource(), item);
            return 1;
        });
    }

    @Override
    public String id() {
        return "itemEditor";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().itemEditor;
    }

    public static class Config extends Feature.Config {}
}

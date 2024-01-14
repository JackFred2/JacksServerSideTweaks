package red.jackf.jsst.mixins.itemeditor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

// easier to use vanilla method than rewrite it without a mixin
@Mixin(SuspiciousStewItem.class)
public interface SuspiciousStewItemAccessor {

    @Invoker("listPotionEffects")
    static void jsst$itemEditor$listPotionEffects(ItemStack stack, Consumer<SuspiciousEffectHolder.EffectEntry> output) {
        throw new AssertionError("mixin apply fail");
    }
}

package red.jackf.jsst.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.features.worldcontainernames.WorldContainerNames;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(
            method = "place",
            at = @At(value = "TAIL")
    )
    private void jsst_updateBlockEntityHook(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel() instanceof ServerLevel serverLevel)
            WorldContainerNames.checkBlockEntity(serverLevel.getBlockEntity(context.getClickedPos()), serverLevel);
    }
}

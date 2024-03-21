package red.jackf.jsst.mixins.campfirestimes;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.feature.campfiretimes.CampfireTimes;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity {
    @Shadow @Final
    private int[] cookingProgress;

    @Shadow @Final
    private int[] cookingTime;

    private CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        throw new AssertionError();
    }

    @Inject(method = "cookTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;cookingProgress:[I"))
    private static void jsst$updateLabels(Level level,
                                          BlockPos pos,
                                          BlockState state,
                                          CampfireBlockEntity blockEntity,
                                          CallbackInfo ci,
                                          @Local(ordinal = 0) int index) {
        if (level instanceof ServerLevel serverLevel) {
            int progress = ((CampfireBlockEntityMixin) (Object) blockEntity).cookingProgress[index];
            int time = ((CampfireBlockEntityMixin) (Object) blockEntity).cookingTime[index];
            CampfireTimes.INSTANCE.tickRecipe(serverLevel, blockEntity, index, progress, time);
        }
    }

    @Inject(method = "dowse", at = @At("HEAD"))
    private void jsst$stopIfNeeded(CallbackInfo ci) {
        if (this.level instanceof ServerLevel) {
            CampfireTimes.INSTANCE.dowse((CampfireBlockEntity) (Object) this);
        }
    }
}

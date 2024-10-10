package red.jackf.jsst.mixins.campfiretimers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jsst.impl.feature.campfiretimers.CampfireTimers;
import red.jackf.jsst.impl.mixinutils.JSSTCampfireExt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin implements JSSTCampfireExt {
    @Shadow @Final private int[] cookingProgress;
    @Shadow @Final private int[] cookingTime;
    @Unique
    private final List<EntityLie<Display.TextDisplay>> lies = new ArrayList<>(Collections.nCopies(4, null));

    @Override
    public List<EntityLie<Display.TextDisplay>> jsst$getLies() {
        return lies;
    }

    @Override
    public int jsst$getCookingProgress(int slot) {
        return this.cookingProgress[slot];
    }

    @Override
    public int jsst$getCookingTime(int slot) {
        return this.cookingTime[slot];
    }

    @Inject(method = "cookTick", at = @At("TAIL"))
    private static void updateLabels(Level level, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            CampfireTimers.cookTick(serverLevel, pos, state, blockEntity, (JSSTCampfireExt) blockEntity);
        }
    }
}

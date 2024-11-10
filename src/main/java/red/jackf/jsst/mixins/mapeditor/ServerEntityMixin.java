package red.jackf.jsst.mixins.mapeditor;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import red.jackf.jsst.impl.feature.mapeditor.MapEditor;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {

    @Shadow @Final private Entity entity;

    @Definition(id = "tickCount", field = "Lnet/minecraft/server/level/ServerEntity;tickCount:I")
    @Expression("this.tickCount % 10 == 0")
    @WrapOperation(method = "sendChanges", at = @At("MIXINEXTRAS:EXPRESSION"), require = 0)
    private boolean fasterUpdateIfMapIsEdited(int tickCountMod10, int zero, Operation<Boolean> original) {
        if (MapEditor.existsSessionUsingFrame((ItemFrame) this.entity)) return true;
        return original.call(tickCountMod10, zero);
    }
}

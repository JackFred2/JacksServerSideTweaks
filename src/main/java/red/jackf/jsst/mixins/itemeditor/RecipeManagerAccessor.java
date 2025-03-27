package red.jackf.jsst.mixins.itemeditor;

import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {

    //? if >=1.21.5 {
    @Accessor("recipes")
    RecipeMap getRecipeMap();
    //?}
}

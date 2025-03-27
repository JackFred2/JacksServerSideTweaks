package red.jackf.jsst.mixins.itemeditor;

//? if >=1.21.5
import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
//? if >=1.21.5
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTrimRecipe.class)
public interface SmithingTrimRecipeAccessor {
    //? if >=1.21.5 {
    @Accessor
    Holder<TrimPattern> getPattern();
    //?}
}

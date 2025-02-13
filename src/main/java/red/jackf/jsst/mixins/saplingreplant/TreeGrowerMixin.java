package red.jackf.jsst.mixins.saplingreplant;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import red.jackf.jsst.impl.feature.saplingreplant.JSSTTreeGrower;

import java.util.Optional;

@Mixin(TreeGrower.class)
public abstract class TreeGrowerMixin implements JSSTTreeGrower {
    @Shadow @Final private Optional<ResourceKey<ConfiguredFeature<?, ?>>> megaTree;

    @Shadow @Final private Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryMegaTree;

    @Shadow @Final private Optional<ResourceKey<ConfiguredFeature<?, ?>>> tree;

    @Shadow @Final private Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryTree;

    @Override
    public boolean jsst$saplingreplant$is2x2Only() {
        return (this.megaTree.isPresent() || this.secondaryMegaTree.isPresent()) && this.tree.isEmpty() && this.secondaryTree.isEmpty();
    }
}

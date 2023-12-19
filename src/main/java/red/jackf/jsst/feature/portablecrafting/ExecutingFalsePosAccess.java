package red.jackf.jsst.feature.portablecrafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record ExecutingFalsePosAccess(Level level) implements ContainerLevelAccess {
    @Override
    public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> levelPosConsumer) {
        return Optional.empty();
    }

    @Override
    public void execute(BiConsumer<Level, BlockPos> levelPosConsumer) {
        levelPosConsumer.accept(this.level, BlockPos.ZERO);
    }
}

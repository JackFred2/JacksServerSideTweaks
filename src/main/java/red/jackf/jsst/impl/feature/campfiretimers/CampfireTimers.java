package red.jackf.jsst.impl.feature.campfiretimers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.mixinutils.JSSTCampfireExt;

import java.util.List;

public class CampfireTimers {
    private static final float OFFSET = 0.35f;
    private static final float HEIGHT = 0.2f;
    private static final float SCALE = 0.75f;

    public static void setup() {}

    public static void cookTick(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity cbe, JSSTCampfireExt ext) {
        List<EntityLie<Display.TextDisplay>> lies = ext.jsst$getLies();

        for (int i = 0; i < cbe.getItems().size(); i++) {
            ItemStack stack = cbe.getItems().get(i);
            if (!stack.isEmpty() && JSSTConfig.INSTANCE.instance().campfireTimers.enabled) {
                if (lies.get(i) == null || lies.get(i).hasFaded()) {
                    EntityLie<Display.TextDisplay> lie = EntityLie.builder(EntityBuilders.textDisplay(level)
                                    .position(getLabelPosition(pos, state, i))
                                    .scale(new Vector3f(SCALE))
                                    .billboard(Display.BillboardConstraints.VERTICAL)
                                    .viewRangeModifier(1f / 16)
                                    .build())
                            .createAndShow();

                    Tracker.<EntityLie<Display.TextDisplay>>builder(level)
                            .setFocus(pos.getCenter(), 12)
                            .addLie(lie)
                            .build(true);

                    lies.set(i, lie);
                }

                EntityUtils.setDisplayText(lies.get(i).entity(), getProgressText(ext.jsst$getCookingProgress(i), ext.jsst$getCookingTime(i)));

                Debris.INSTANCE.schedule(lies.get(i), 1L);
            } else {
                EntityLie<Display.TextDisplay> existing = lies.set(i, null);
                if (existing != null) {
                    existing.fade();
                }
            }
        }
    }

    private static Component getProgressText(int cookingProgress, int cookingTime) {
        return Component.literal("%.0f%%".formatted(100f * cookingProgress / cookingTime));
    }

    private static Vec3 getLabelPosition(BlockPos pos, BlockState state, int slot) {
        Direction facing = state.getValue(CampfireBlock.FACING);

        float rotation = (facing.get2DDataValue() + slot + 2) * -Mth.HALF_PI;

        Vec3 offset = new Vec3(OFFSET, HEIGHT, OFFSET).yRot(rotation);

        return pos.getCenter().add(offset);
    }
}

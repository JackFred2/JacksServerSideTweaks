package red.jackf.jsst.impl.feature.concealableframes;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.mixins.concealableframes.ItemFrameAccessor;

import java.util.stream.StreamSupport;

public class ConcealableFrames {
    public static void setup() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, entityHitResult) -> {
            if (JSSTConfig.INSTANCE.instance().concealableFrames.enabled
                    && player instanceof ServerPlayer serverPlayer
                    && player.isCrouching()
                    && entityHitResult == null
                    && isTool(serverPlayer.getItemInHand(hand))
                    && entity instanceof ItemFrame frame
                    && !((ItemFrameAccessor) frame).jsst$isFixed()) {
                entity.setInvisible(!entity.isInvisible());
                level.playSound(null, entity.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }

    private static boolean isTool(ItemStack stack) {
        return stack.is(Items.POTION) && StreamSupport.stream(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .getAllEffects().spliterator(), false)
                .anyMatch(instance -> instance.is(MobEffects.INVISIBILITY));
    }

    public static void onSurvivesTick(ItemFrame frame) {
        if (JSSTConfig.INSTANCE.instance().concealableFrames.showEmptyParticles
            && frame.isInvisible() && frame.getItem().isEmpty() && frame.level() instanceof ServerLevel serverLevel) {
            double x = frame.getX();
            double y = frame.getY();
            double z = frame.getZ();

            Direction dir = frame.getDirection();

            double xOffset = dir.getStepX() != 0 ? 0 : 0.1;
            double yOffset = dir.getStepY() != 0 ? 0 : 0.1;
            double zOffset = dir.getStepZ() != 0 ? 0 : 0.1;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x - xOffset,
                    y - yOffset,
                    z - zOffset,
                    6,
                    xOffset * 2,
                    yOffset * 2,
                    zOffset * 2,
                    0.0
            );

        }
    }
}

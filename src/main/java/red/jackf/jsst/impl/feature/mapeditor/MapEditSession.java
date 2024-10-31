package red.jackf.jsst.impl.feature.mapeditor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;
import red.jackf.jsst.impl.utils.Sounds;

public final class MapEditSession {
    private static final float FRAME_DEPTH = 0.0625f;

    private final ServerPlayer player;
    private final ItemFrame frame;
    @Nullable
    private EntityGlowLie<ItemFrame> glowLie = null;
    @Nullable
    private EntityLie<Interaction> interactionLie = null;

    public MapEditSession(ServerPlayer player, ItemFrame frame) {
        this.player = player;
        this.frame = frame;
    }

    public ServerPlayer player() {
        return player;
    }

    public ItemFrame entity() {
        return frame;
    }

    public boolean stillValid() {
        return !player.isRemoved()
                && !frame.isRemoved()
                && MapEditor.isValidTool(player.serverLevel()
                .registryAccess(), player.getItemInHand(InteractionHand.MAIN_HAND))
                && player.getEyePosition().distanceTo(frame.position()) < 8;
    }

    public void start() {
        this.glowLie = EntityGlowLie.builder(frame)
                .colour(ChatFormatting.GREEN)
                .createAndShow(player);

        this.interactionLie = EntityLie.builder(EntityBuilders.interaction(this.player().serverLevel())
                        .width(1f)
                        .height(1f)
                        .position(getFrontFaceCenter().relative(this.frame.getDirection().getOpposite(), 0.5f).subtract(0, 0.5, 0))
                        .build())
                .onLeftClick(this::onLeftClickFrame)
                .createAndShow(player);

        Sounds.Ding.ding(player);
    }

    private Vec3 getFrontFaceCenter() {
        return this.entity().position().relative(this.frame.getDirection(), 0.033f);
    }

    private Vec2 toLocal(Vec3 worldPos) {
        Direction dir = this.entity().getDirection();

        Vector3f withItemRot = worldPos.subtract(getFrontFaceCenter()).toVector3f()
                .rotateAxis((this.entity().getRotation() % 4) * Mth.HALF_PI, dir.getStepX(), dir.getStepY(), dir.getStepZ());

        Vector3f local = switch (dir) {
            case UP -> withItemRot;
            case DOWN -> withItemRot.rotateX(Mth.PI);
            default -> withItemRot.rotateY(dir.get2DDataValue() * Mth.HALF_PI).rotateX(-Mth.HALF_PI);
        };

        return new Vec2(local.x, local.z);
    }

    private Vec3 toWorld(Vec2 localPos) {
        Direction dir = this.entity().getDirection();

        Vector3f vec3 = new Vector3f(localPos.x, 0f, localPos.y);

        Vector3f worldNonRotated = switch (dir) {
            case UP -> vec3;
            case DOWN -> vec3.rotateX(-Mth.PI);
            default -> vec3.rotateX(Mth.HALF_PI).rotateY(dir.get2DDataValue() * -Mth.HALF_PI);
        };

        Vector3f world = worldNonRotated
                .rotateAxis((this.entity().getRotation() % 4) * -Mth.HALF_PI, dir.getStepX(), dir.getStepY(), dir.getStepZ())
                .add(getFrontFaceCenter().toVector3f());

        return new Vec3(world);
    }

    private void onLeftClickFrame(ServerPlayer player, EntityLie<Interaction> lie, boolean sneaking, Vec3 pos) {
        Vec2 local = toLocal(pos);
        player.sendSystemMessage(Component.literal("Pos: %.2f, %.2f".formatted(local.x, local.y)));

        Vec3 reverse = toWorld(local);
        player.sendSystemMessage(Component.literal("Orig: %s".formatted(pos)));
        player.sendSystemMessage(Component.literal("Remade: %s".formatted(reverse)));
    }

    public void tick() {

    }

    public void end() {
        if (this.glowLie != null) this.glowLie.fade();
        if (this.interactionLie != null) this.interactionLie.fade();

        Sounds.Ding.fail(player);
    }

    public void onHit(EntityHitResult hit) {
        assert hit.getEntity() == this.frame;
    }
}

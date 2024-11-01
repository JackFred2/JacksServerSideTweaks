package red.jackf.jsst.impl.feature.mapeditor;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jackfredlib.api.lying.entity.builders.display.ItemDisplayBuilder;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.mixins.mapeditor.MapItemSavedDataAccessor;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapEditSession {
    public static final String KEY = "jsstCustom";
    private static final Set<Holder<MapDecorationType>> MANAGED = Set.of(
            MapDecorationTypes.PLAYER,
            MapDecorationTypes.FRAME
    );
    private static final List<Holder<MapDecorationType>> AVAILABLE = List.of(
            MapDecorationTypes.TARGET_X,
            MapDecorationTypes.RED_X,
            MapDecorationTypes.TARGET_POINT,
            MapDecorationTypes.PLAINS_VILLAGE,
            MapDecorationTypes.DESERT_VILLAGE,
            MapDecorationTypes.TAIGA_VILLAGE,
            MapDecorationTypes.SNOWY_VILLAGE,
            MapDecorationTypes.SAVANNA_VILLAGE,
            MapDecorationTypes.WOODLAND_MANSION,
            MapDecorationTypes.OCEAN_MONUMENT,
            MapDecorationTypes.TRIAL_CHAMBERS,
            MapDecorationTypes.JUNGLE_TEMPLE,
            MapDecorationTypes.SWAMP_HUT
    );
    private static final float BUTTON_SIZE = 0.06f;
    private static final float BUTTON_SPACING = 0.1f;
    private static final float HEAD_SCALE_FACTOR = 1.5f;

    private final ServerPlayer player;
    private final ItemFrame frame;
    private final MapId mapId;
    @Nullable
    private EntityGlowLie<ItemFrame> glowLie = null;
    @Nullable
    private EntityLie<Interaction> mainPlane = null;
    @Nullable
    private EntityLie<Display.ItemDisplay> decoHighlight = null;
    @Nullable
    private String currentlyInteractedId = null;
    private boolean ended = false;

    public MapEditSession(ServerPlayer player, ItemFrame frame, MapId mapId) {
        this.player = player;
        this.frame = frame;
        this.mapId = mapId;
    }

    public ServerPlayer player() {
        return player;
    }

    public ItemFrame entity() {
        return frame;
    }

    public MapId getMapId() {
        return mapId;
    }

    private MapItemSavedData getMapData() {
        return this.player.serverLevel().getMapData(this.mapId);
    }

    public boolean stillValid() {
        return !player.isRemoved()
                && !frame.isRemoved()
                && !this.ended
                && MapEditor.isValidTool(player.serverLevel().registryAccess(), player.getItemInHand(InteractionHand.MAIN_HAND))
                && player.getEyePosition().distanceTo(frame.position()) < 8;
    }

    public void end() {
        if (this.glowLie != null) this.glowLie.fade();
        if (this.mainPlane != null) this.mainPlane.fade();
        if (this.decoHighlight != null) this.decoHighlight.fade();

        this.ended = true;
        this.currentlyInteractedId = null;

        Sounds.Ding.fail(player);
    }

    public void start() {
        this.glowLie = EntityGlowLie.builder(frame)
                .colour(ChatFormatting.GREEN)
                .createAndShow(player);

        this.mainPlane = EntityLie.builder(EntityBuilders.interaction(this.player().serverLevel())
                        .width(1f)
                        .height(1f)
                        .position(getFrontFaceCenter().relative(this.frame.getDirection().getOpposite(), 0.5f).subtract(0, 0.5, 0))
                        .build())
                .onLeftClick((player1, lie, wasSneaking, relativeToEntity) -> onLeftClickFrame(wasSneaking, relativeToEntity))
                .onRightClick((player1, lie, wasSneaking, hand, relativeToEntity) -> onRightClickFrame(wasSneaking, hand, relativeToEntity))
                .createAndShow(player);

        Sounds.Ding.ding(player);
    }

    /**
     * Returns a triple of a world position and base entity y and x rotations for a UI element.
     *
     * @param origin Map origin point to base around (i.e. the decoration point in scaled down coordinates -0.5 < 0.5)
     * @param offset Offset to 'origin' in scaled down map coordinates
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private UILocation getUILocation(Vec2 origin, Vec2 offset) {
        Direction dir = this.entity().getDirection();
        int rot = this.entity().getRotation() % 4;

        if (dir.getAxis().isHorizontal()) {
            offset = switch (rot) {
                case 1 -> new Vec2(offset.y, -offset.x);
                case 2 -> offset.negated();
                case 3 -> new Vec2(-offset.y, offset.x);
                default -> offset;
            };
        }

        Vec3 world = toWorld(origin.add(offset)).relative(this.entity().getDirection(), 0.01);

        float xRot = switch (dir) {
            case UP -> -90;
            case DOWN -> 90;
            default -> 0;
        };

        float yRot = switch (dir) {
            case UP -> rot * 90;
            case DOWN -> rot * -90;
            default -> 90 * dir.get2DDataValue();
        };

        return new UILocation(world, yRot, xRot);
    }

    private ItemDisplayBuilder createUIIcon(UILocation location, ItemStack stack, Colour glowColour) {
        float size = BUTTON_SIZE * (stack.getItem() instanceof PlayerHeadItem ? HEAD_SCALE_FACTOR : 1f);

        return EntityBuilders.itemDisplay(this.player().serverLevel())
                .position(location.worldPos)
                .yRotation(location.yRot)
                .xRotation(location.xRot)
                .scale(new Vector3f(size, size, size * 0.1f))
                .displayContext(ItemDisplayContext.FIXED)
                .brightness(15, 15)
                .glowing(true, glowColour)
                .stack(stack);
    }

    private Interaction createInteractForItem(Display.ItemDisplay item) {
        return EntityBuilders.interaction(this.player().serverLevel())
                .width(BUTTON_SIZE)
                .height(BUTTON_SIZE)
                .response(true)
                .position(item.position().subtract(0, BUTTON_SIZE / 2, 0))
                .build();
    }

    private <T extends Entity> EntityLie.TickCallback<T> getValid(String id) {
        return (player1, lie) -> {
            if (!id.equals(this.currentlyInteractedId)) {
                lie.fade();
            }
        };
    }

    private void createUIButton(ItemDisplayBuilder item, Runnable onClick) {
        String currentId = this.currentlyInteractedId;

        EntityLie<Display.ItemDisplay> visual = EntityLie.builder(item.build())
                .onTick(getValid(currentId))
                .createAndShow(player);

        EntityLie.builder(createInteractForItem(visual.entity()))
                .onTick(getValid(currentId))
                .onLeftClick(((player1, lie, wasSneaking, relativeToEntity) -> this.deselect()))
                .onRightClick((player1, lie, wasSneaking, hand, relativeToEntity) -> onClick.run())
                .createAndShow(player);
    }

    private void deselect() {
        this.currentlyInteractedId = null;

        if (this.glowLie != null) this.glowLie.setGlowColour(ChatFormatting.GREEN);
    }

    private void selectForEdit(String id) {
        var deco = getDecorationById(id);
        if (deco == null) return;

        if (this.currentlyInteractedId != null) {
            this.deselect();
        }

        if (this.glowLie != null) this.glowLie.setGlowColour(null);

        this.currentlyInteractedId = id;

        Vec2 origin = new Vec2(deco.x() / 256f, deco.y() / 256f);

        this.decoHighlight = EntityLie.builder(createUIIcon(getUILocation(origin, Vec2.ZERO), Items.LIGHT_BLUE_STAINED_GLASS_PANE.getDefaultInstance(), Colours.LIGHT_BLUE)
                        .scale(new Vector3f(0.07f))
                        .setTranslation(new Vector3f(0, 0, -0.01f))
                        .build())
                .onTick(getValid(id))
                .createAndShow(player);

        // next
        createUIButton(createUIIcon(getUILocation(origin, new Vec2(BUTTON_SPACING, 0)), Heads.RIGHT_ARROW, Colours.GREEN), () -> {

        });

        // prev
        createUIButton(createUIIcon(getUILocation(origin, new Vec2(-BUTTON_SPACING, 0)), Heads.LEFT_ARROW, Colours.GREEN), () -> {

        });

        // rename
        createUIButton(createUIIcon(getUILocation(origin, new Vec2(0, -BUTTON_SPACING)), Items.NAME_TAG.getDefaultInstance(), Colours.YELLOW), () -> {

        });

        // delete
        createUIButton(createUIIcon(getUILocation(origin, new Vec2(0, BUTTON_SPACING)), Items.BARRIER.getDefaultInstance(), Colours.RED), () -> {

        });

        for (int i = 0; i < 16; i ++) {
            float rotation = 22.5f * i;
            float rotationRad = rotation * Mth.DEG_TO_RAD;
            float visualRotation = rotationRad + 0.75f * Mth.PI;

            Colour col = Colour.fromHSV(((rotation + 180) % 360) / 360f, 0.7f, 1f);

            createUIButton(createUIIcon(getUILocation(origin, new Vec2(-Mth.sin(rotationRad), Mth.cos(rotationRad)).scale(2 * BUTTON_SPACING)), Items.IRON_SWORD.getDefaultInstance(), col)
                    .leftRotation(new Quaternionf(new AxisAngle4f(visualRotation, new Vector3f(0, 0, -1)))), () -> {
                player.sendSystemMessage(Component.literal("%.1f".formatted(rotation)));
            });
        }
    }

    private void onRightClickFrame(boolean sneaking, InteractionHand hand, Vec3 hit) {
        MapItemSavedData data = this.getMapData();
        Vec2 local = this.toLocal(hit.add(this.entity().position().subtract(0, 0.5, 0)));

        var hovered = getHoveredDecoration(local);

        if (hovered != null) {
            selectForEdit(hovered.getFirst());
        } else {
            Vector2i blockCoords = getWorldPosition(local);
            String id = KEY + "/%d %d".formatted(blockCoords.x, blockCoords.y);

            ((MapItemSavedDataAccessor) data).invokeAddDecoration(AVAILABLE.getFirst(),
                    this.player().serverLevel(),
                    id,
                    blockCoords.x,
                    blockCoords.y,
                    180d,
                    null);

            selectForEdit(id);
        }
    }

    private void onLeftClickFrame(boolean sneaking, Vec3 hit) {
        if (this.currentlyInteractedId != null) {
            this.deselect();
        } else {
            this.end();
        }
    }

    private Vec3 getFrontFaceCenter() {
        return this.entity().position().relative(this.frame.getDirection(), 0.035f);
    }

    private Vec2 toLocal(Vec3 worldPos) {
        Direction dir = this.entity().getDirection();

        Vector3f withItemRot = worldPos.subtract(getFrontFaceCenter())
                .toVector3f()
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

    private Vector2i getWorldPosition(Vec2 localPos) {
        var mapData = this.getMapData();

        int scale = 128 * (1 << mapData.scale);

        // we subtract 1 from the X to fix offset that idk the source of
        return new Vector2i((int) (mapData.centerX + localPos.x * scale) - 1, (int) (mapData.centerZ + localPos.y * scale));
    }

    private @Nullable MapDecoration getDecorationById(String id) {
        return ((MapItemSavedDataAccessor) this.getMapData()).getDecorations().get(id);
    }

    private @Nullable Pair<String, MapDecoration> getHoveredDecoration(Vec2 local) {
        for (Map.Entry<String, MapDecoration> decoration : ((MapItemSavedDataAccessor) this.getMapData()).getDecorations().entrySet()) {
            Vec2 decoLocal = new Vec2(decoration.getValue().x() / 256f, decoration.getValue().y() / 256f);

            if (decoLocal.distanceToSqr(local) < 1f / 384) {
                // dont interact with player markers or item frame icons
                if (MANAGED.contains(decoration.getValue().type())) continue;

                // dont interact with banner markers
                if (this.getMapData().getBanners().stream().anyMatch(banner -> banner.getId().equals(decoration.getKey()))) continue;

                return Pair.of(decoration.getKey(), decoration.getValue());
            }
        }

        return null;
    }

    private record UILocation(Vec3 worldPos, float yRot, float xRot) {
    }
}

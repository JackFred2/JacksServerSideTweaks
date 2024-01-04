package red.jackf.jsst.mixins.itemeditor;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.feature.itemeditor.ItemEditor;
import red.jackf.jsst.feature.itemeditor.previousColours.EditorColourHistory;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements EditorColourHistory {
    @Unique
    private static final String KEY_PREVIOUS_COLOURS = "JSSTPreviousColours";
    @Unique
    private static final String KEY_PREVIOUS_GRADIENTS = "JSSTPreviousGradients";
    @Unique
    private final List<Colour> previousColours = new ArrayList<>();
    @Unique
    private final List<Gradient> previousGradients = new ArrayList<>();

    @Override
    public List<Colour> jsst$itemEditor$getPreviousColours() {
        return previousColours;
    }

    @Override
    public List<Gradient> jsst$itemEditor$getPreviousGradients() {
        return previousGradients;
    }

    @Override
    public void jsst$itemEditor$push(Gradient gradient) {
        if (gradient instanceof Colour colour) {
            int existingIndex = previousColours.indexOf(colour);
            if (existingIndex != -1) {
                previousColours.remove(existingIndex);
            }

            previousColours.add(0, colour);

            while (previousColours.size() > 16) previousColours.remove(16);
        } else {
            int existingIndex = previousGradients.indexOf(gradient);
            if (existingIndex != -1) {
                previousGradients.remove(existingIndex);
            }

            previousGradients.add(0, gradient);

            while (previousGradients.size() > 16) previousGradients.remove(16);
        }
    }

    @Override
    public void jsst$itemEditor$copyFrom(ServerPlayer oldPlayer) {
        this.previousColours.clear();
        this.previousColours.addAll(((EditorColourHistory) oldPlayer).jsst$itemEditor$getPreviousColours());
        this.previousGradients.clear();
        this.previousGradients.addAll(((EditorColourHistory) oldPlayer).jsst$itemEditor$getPreviousGradients());
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void jsst$itemEditor$readPreviousColours(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(KEY_PREVIOUS_COLOURS))
            Colour.CODEC.listOf()
                        .parse(new Dynamic<>(NbtOps.INSTANCE, tag.get(KEY_PREVIOUS_COLOURS)))
                        .resultOrPartial(ItemEditor.LOGGER::error)
                        .ifPresent(this.previousColours::addAll);

        if (tag.contains(KEY_PREVIOUS_GRADIENTS))
            Gradient.CODEC.listOf()
                        .parse(new Dynamic<>(NbtOps.INSTANCE, tag.get(KEY_PREVIOUS_GRADIENTS)))
                        .resultOrPartial(ItemEditor.LOGGER::error)
                        .ifPresent(this.previousGradients::addAll);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void jsst$itemEditor$savePreviousColours(CompoundTag tag, CallbackInfo ci) {
        if (!this.previousColours.isEmpty())
            Colour.CODEC.listOf()
                        .encodeStart(NbtOps.INSTANCE, this.previousColours)
                        .resultOrPartial(ItemEditor.LOGGER::error)
                        .ifPresent(listTag -> tag.put(KEY_PREVIOUS_COLOURS, listTag));

        if (!this.previousGradients.isEmpty())
            Gradient.CODEC.listOf()
                        .encodeStart(NbtOps.INSTANCE, this.previousGradients)
                        .resultOrPartial(ItemEditor.LOGGER::error)
                        .ifPresent(listTag -> tag.put(KEY_PREVIOUS_GRADIENTS, listTag));
    }
}

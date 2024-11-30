package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import com.mojang.serialization.DataResult;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.function.Consumer;

public class GiveCommandGenerator implements Editor {
    public static final Type<GiveCommandGenerator> TYPE = Editor.<GiveCommandGenerator>typeBuilder(JSST.id("give_command_generator"))
            .factory(GiveCommandGenerator::new)
            .labelFactory(GiveCommandGenerator::getLabel)
            .inputHint(Translations::generate)
            .developer()
            .supportsCosmetic()
            .build();
    private final EditSession session;
    private final Consumer<Result> callback;

    private static GuiElementInterface getLabel(EditSession session) {
        return JSSTElementBuilder.from(Items.COMMAND_BLOCK).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.giveCommandGenerator"))
                .build();
    }

    public GiveCommandGenerator(EditSession session, Consumer<Result> callback) {
        this.session = session;
        this.callback = callback;
    }

    @Override
    public void start() {
        Sounds.UI.click(this.session.getPlayer());

        var string = this.generateString();

        string.ifSuccess(command -> {
            Sounds.UI.click(this.session.getPlayer());

            this.session.getPlayer().sendSystemMessage(Styles.clipboardCopy(command));
        }).ifError(err -> {
            Sounds.UI.close(this.session.getPlayer());

            this.session.getPlayer().sendSystemMessage(Component.translatable("jsst.itemEditor.editor.giveCommandGenerator.error", err.message()));
        });

        this.callback.accept(Result.empty());
    }

    private DataResult<String> generateString() {
        ItemStack stack = this.session.getStack();
        DataComponentPatch.SplitResult patch = stack.getComponentsPatch().split();
        int count = stack.getComponentsPatch().size();

        StringBuilder str = new StringBuilder();

        str.append("/give @s ");

        @Nullable ResourceLocation key = RegistryUtils.lookup(this.session.registries(), Registries.ITEM)
                .getKey(stack.getItem());

        if (key != null) {
            str.append(key);

            if (!stack.getComponentsPatch().isEmpty()) {
                str.append("[");

                for (DataComponentType<?> removed : patch.removed()) {
                    str.append("!");
                    str.append(removed);
                    if (--count > 0) str.append(",");
                }

                for (TypedDataComponent<?> typed : patch.added()) {
                    DataResult<String> encoded = makeForTypeValue(typed);

                    if (encoded.isError()) {
                        //noinspection OptionalGetWithoutIsPresent
                        return DataResult.error(() -> "Error adding component: " + encoded.error().get().message());
                    }

                    str.append(typed.type());
                    str.append("=");
                    str.append(encoded.getOrThrow());
                    if (--count > 0) str.append(",");
                }

                str.append("]");
            }

            return DataResult.success(str.toString());
        } else {
            return DataResult.error(() -> "No ID for item");
        }
    }

    private <T> DataResult<String> makeForTypeValue(TypedDataComponent<T> typed) {
        RegistryOps<Tag> ops = this.session.registries().createSerializationContext(NbtOps.INSTANCE);
        return typed.encodeValue(ops).map(Tag::getAsString);
    }
}

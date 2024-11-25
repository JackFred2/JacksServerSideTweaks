package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import com.mojang.authlib.properties.PropertyMap;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerHeadNameEditor implements Editor {
    public static Type<PlayerHeadNameEditor> TYPE = Editor.<PlayerHeadNameEditor>typeBuilder(JSST.id("player_head_name"))
            .factory(PlayerHeadNameEditor::new)
            .labelFactory(PlayerHeadNameEditor::getIcon)
            .appliesTo(session -> session.getStack().is(Items.PLAYER_HEAD))
            .supportsCosmetic()
            .inputHint(Translations::change)
            .build();
    private final EditSession session;
    private final Consumer<Result> result;

    public PlayerHeadNameEditor(EditSession session, Consumer<Result> result) {
        this.session = session;
        this.result = result;
    }

    private static GuiElementInterface getIcon(EditSession session) {
        return JSSTElementBuilder.from(Items.PLAYER_HEAD).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.playerHeadName"))
                .build();
    }

    private static ResolvableProfile makeProfile(String name) {
        return new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap());
    }

    private static ResolvableProfile makeProfile(UUID id) {
        return new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap());
    }

    @Override
    public void start() {
        Sounds.UI.click(this.session.getPlayer());

        InputMenus.string(this.session.getPlayer())
                .validator(str -> {
                    if (StringUtil.isValidPlayerName(str)) {
                        return true;
                    } else {
                        try {
                            UUID.fromString(str);
                            return true;
                        } catch (IllegalArgumentException ex) {
                            return false;
                        }
                    }
                })
                .hint(Component.translatable("jsst.itemEditor.validFormats"),
                        Component.literal("- §aUsername"),
                        Component.literal("- §a57dd3ebe-1525-4c07-a045-5a508f501ae8 §r(UUID)"))
                .start(opt -> {
                    if (opt.isPresent()) {
                        ResolvableProfile profile;
                        if (StringUtil.isValidPlayerName(opt.get())) {
                            profile = makeProfile(opt.get());
                        } else {
                            profile = makeProfile(UUID.fromString(opt.get()));
                        }

                        ItemStack stack = this.session.getStack();
                        stack.set(DataComponents.PROFILE, profile);
                        result.accept(Result.of(stack));
                    } else {
                        result.accept(Result.empty());
                    }
                });
    }
}

package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.resources.ResourceLocation;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Editor {
    void start();

    interface Factory<E extends Editor> {
        E create(EditSession session, Consumer<Result> resultConsumer);
    }

    class Type<E extends Editor> {
        private final ResourceLocation id;
        private final GuiEditor.Factory<E> factory;
        private final Function<EditSession, GuiElementInterface> iconFactory;
        private final Predicate<EditSession> appliesTo;

        public Type(ResourceLocation id, GuiEditor.Factory<E> factory, Predicate<EditSession> appliesTo, Function<EditSession, GuiElementInterface> iconFactory) {
            this.id = id;
            this.factory = factory;
            this.iconFactory = iconFactory;
            this.appliesTo = appliesTo;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public E create(EditSession session, Consumer<Result> resultConsumer) {
            return this.factory.create(session, resultConsumer);
        }

        public GuiElementInterface getIcon(EditSession session) {
            return this.iconFactory.apply(session);
        }

        public boolean appliesTo(EditSession session) {
            return this.appliesTo.test(session);
        }
    }
}

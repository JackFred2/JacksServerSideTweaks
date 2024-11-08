package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.sgui.Translations;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Editor {
    void start();

    interface Factory<E extends Editor> {
        E create(EditSession session, Consumer<Result> resultConsumer);
    }

    static <E extends Editor> Type.Builder<E> typeBuilder(ResourceLocation id) {
        return new Type.Builder<>(id);
    }

    class Type<E extends Editor> {
        private final ResourceLocation id;
        private final boolean developer;
        private final boolean supportsCosmetic;
        private final Supplier<Component> inputHint;
        private final GuiEditor.Factory<E> factory;
        private final Predicate<EditSession> predicate;
        private final Function<EditSession, GuiElementInterface> labelFactory;

        private Type(ResourceLocation id,
                     Predicate<EditSession> predicate,
                     boolean developer,
                     boolean supportsCosmetic,
                     Supplier<Component> inputHint,
                     Function<EditSession, GuiElementInterface> labelFactory,
                     Factory<E> factory) {
            this.id = id;
            this.developer = developer;
            this.supportsCosmetic = supportsCosmetic;
            this.inputHint = inputHint;
            this.factory = factory;
            this.predicate = predicate;
            this.labelFactory = labelFactory;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public E create(EditSession session, Consumer<Result> resultConsumer) {
            return this.factory.create(session, resultConsumer);
        }

        public boolean appliesTo(EditSession session) {
            return this.predicate.test(session);
        }

        public GuiElementInterface getLabel(EditSession session) {
            return this.labelFactory.apply(session);
        }

        public Component getInputHint() {
            return this.inputHint.get();
        }

        public boolean isDeveloper() {
            return developer;
        }

        public boolean supportsCosmetic() {
            return supportsCosmetic;
        }

        public static class Builder<E extends Editor> {
            private final ResourceLocation id;
            private Factory<E> factory;
            private Predicate<EditSession> predicate = s -> true;
            private Function<EditSession, GuiElementInterface> labelFactory;
            private boolean developer = false;
            private boolean supportsCosmetic = false;
            private Supplier<Component> inputHint = Translations::open;

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder<E> factory(Factory<E> factory) {
                this.factory = factory;
                return this;
            }

            public Builder<E> appliesTo(Predicate<EditSession> predicate) {
                this.predicate = predicate;
                return this;
            }

            public Builder<E> labelFactory(Function<EditSession, GuiElementInterface> labelFactory) {
                this.labelFactory = labelFactory;
                return this;
            }

            public Builder<E> developer() {
                this.developer = true;
                return this;
            }

            public Builder<E> supportsCosmetic() {
                this.supportsCosmetic = true;
                return this;
            }

            public Builder<E> inputHint(Supplier<Component> inputHint) {
                this.inputHint = inputHint;
                return this;
            }

            public Type<E> build() {
                Objects.requireNonNull(factory, "null factory");
                Objects.requireNonNull(labelFactory, "null label factory");
                return new Type<>(id, predicate, developer, supportsCosmetic, inputHint, labelFactory, factory);
            }
        }
    }
}

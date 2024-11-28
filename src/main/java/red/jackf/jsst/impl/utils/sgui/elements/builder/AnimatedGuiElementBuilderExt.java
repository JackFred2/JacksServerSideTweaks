package red.jackf.jsst.impl.utils.sgui.elements.builder;

import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.utils.sgui.elements.WrappedElement;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMap;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AnimatedGuiElementBuilderExt extends AnimatedGuiElementBuilder {
    public AnimatedGuiElementBuilderExt addStack(ItemStack stack) {
        this.itemStacks.add(stack);
        return this;
    }

    @Override
    public AnimatedGuiElementBuilderExt setInterval(int interval) {
        super.setInterval(interval);
        return this;
    }

    @Override
    public AnimatedGuiElementBuilderExt setRandom(boolean value) {
        super.setRandom(value);
        return this;
    }

    public static <T> AnimatedGuiElementBuilderExt makeForEach(Iterable<T> elements, LabelMap<T> factory) {
        return makeForEach(StreamSupport.stream(elements.spliterator(), false), factory);
    }

    public static <T> AnimatedGuiElementBuilderExt makeForEach(Stream<T> elements, LabelMap<T> factory) {
        var builder = new AnimatedGuiElementBuilderExt();

        elements.map(factory).forEach(builder::addStack);

        return builder;
    }

    public WrappedElement.Builder<AnimatedGuiElement> wrap() {
        return WrappedElement.builder(this.build());
    }
}

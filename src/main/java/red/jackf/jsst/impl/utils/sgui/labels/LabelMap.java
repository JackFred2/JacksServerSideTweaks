package red.jackf.jsst.impl.utils.sgui.labels;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.GridPaginator;
import red.jackf.jsst.impl.utils.sgui.labels.datapack.DatapackLabelMap;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface LabelMap<T> extends Function<T, ItemStack>, GridPaginator.DrawFunction<T> {
    static <T> LabelMap<Holder<T>> createDatapacked(ResourceKey<Registry<T>> registryKey, Function<Holder<T>, ItemStack> defaultFunction, BiFunction<Holder<T>, ItemStack, ItemStack> posProcess) {
        return DatapackLabelMap.create(registryKey, defaultFunction, posProcess);
    }

    ItemStack apply(T option);

    @Override
    default GuiElementInterface draw(int elementIndex, T element) {
        return JSSTElementBuilder.from(this.apply(element)).build();
    }
}

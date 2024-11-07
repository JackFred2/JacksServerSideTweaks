package red.jackf.jsst.impl.utils.sgui.labels.registry;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RegistryLabelMap<T> implements LabelMap<Holder<T>> {
    private final Map<Holder<T>, ItemStack> map = new HashMap<>();
    private final Function<Holder<T>, ItemStack> defaultFunction;
    private final BiFunction<Holder<T>, ItemStack, ItemStack> postProcess;

    protected RegistryLabelMap(Function<Holder<T>, ItemStack> defaultFunction, BiFunction<Holder<T>, ItemStack, ItemStack> postProcess) {
        this.defaultFunction = defaultFunction;
        this.postProcess = postProcess;
    }

    public static <T> LabelMap<Holder<T>> create(ResourceKey<Registry<T>> registryKey, Function<Holder<T>, ItemStack> defaultFunction, BiFunction<Holder<T>, ItemStack, ItemStack> postProcess) {
        RegistryLabelMap<T> map = new RegistryLabelMap<>(defaultFunction, postProcess);

        ResourceLocation id = JSST.id("registry_label_map/" + registryKey.location().getPath());

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(id, provider -> new RegistryLabelLoader<>(id, registryKey, provider, map));

        return map;
    }

    @Override
    public ItemStack apply(Holder<T> option) {
        ItemStack label = this.map.get(option);

        if (label == null) {
            label = this.defaultFunction.apply(option);
        }

        return JSSTElementBuilder.from(this.postProcess.apply(option, label)).ui().hideDefaultTooltip().asStack();
    }

    public void reload(RegistryLabelLoader.LoadResult<T> reloadData) {
        this.map.clear();
        this.map.putAll(reloadData.labelMap());
    }
}

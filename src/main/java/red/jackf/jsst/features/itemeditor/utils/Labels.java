package red.jackf.jsst.features.itemeditor.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.itemeditor.editors.LoreEditor;

import java.util.ArrayList;
import java.util.List;

public class Labels {
    public static final Style HINT = Style.EMPTY.withColor(TextColor.parseColor("#70FF68")).withItalic(false);
    public static final Style CLEAN = Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false);

    public static LabelBuilder create(ItemStack stack) {
        return new LabelBuilder(stack);
    }

    public static LabelBuilder create(ItemLike item) {
        return new LabelBuilder(new ItemStack(item));
    }

    public static ItemStack blank() {
        return ItemStack.EMPTY;
    }

    public static class LabelBuilder {
        private final ItemStack stack;
        @Nullable
        private MutableComponent customName = null;
        private final List<String> hints = new ArrayList<>();
        private boolean keepLore = false;
        private Style style = Style.EMPTY;

        private LabelBuilder(ItemStack stack) {
            this.stack = stack;
        }

        public LabelBuilder withName(Component name) {
            this.customName = name.copy();
            return this;
        }

        public LabelBuilder withName(MutableComponent name) {
            this.customName = name;
            return this;
        }

        public LabelBuilder withName(String name) {
            this.customName = Component.literal(name).withStyle(CLEAN);
            return this;
        }

        public LabelBuilder withHint(String hint) {
            this.hints.add(hint);
            return this;
        }

        public LabelBuilder addStyle(Style style) {
            this.style = style.applyTo(this.style);
            return this;
        }

        public LabelBuilder keepLore() {
            this.keepLore = true;
            return this;
        }

        public ItemStack build() {
            var result = stack.copy();
            if (customName != null) result.setHoverName(customName.withStyle(style));
            if (hints.size() > 0) result = LoreEditor.mergeLore(result, hints.stream().map(s -> (Component) Component.literal(s).withStyle(HINT)).toList());
            if (!keepLore)
                for (ItemStack.TooltipPart part : ItemStack.TooltipPart.values())
                    result.hideTooltipPart(part);
            return result;
        }
    }
}

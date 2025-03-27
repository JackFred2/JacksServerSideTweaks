package red.jackf.jsst.impl.utils.sgui.elements.builder;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
//? if <=1.21.4
/*import net.minecraft.util.Unit;*/
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.utils.sgui.Hints;
import red.jackf.jsst.impl.utils.sgui.Inputs;
import red.jackf.jsst.impl.utils.sgui.Styles;

import java.util.function.UnaryOperator;

public class JSSTElementBuilder implements GuiElementBuilderInterface<JSSTElementBuilder> {
    private ItemStack stack;

    private boolean cleanText = true;
    private GuiElementInterface.ClickCallback callback = (a, b, c, d) -> {};
    private boolean isUIElement = false;

    private JSSTElementBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public static JSSTElementBuilder from(ItemStack stack) {
        return new JSSTElementBuilder(stack.copy());
    }

    public static JSSTElementBuilder from(ItemLike item) {
        return new JSSTElementBuilder(item.asItem().getDefaultInstance());
    }

    /**
     * Creates a builder from the given stack, intended for use as a UI element with no extraneous info such as lore or tooltip details
     */
    public static JSSTElementBuilder flatCopy(ItemStack stack) {
        return from(stack).ui()
                .hideDefaultTooltip()
                .removeComponent(DataComponents.LORE);
    }

    public JSSTElementBuilder setItem(ItemLike item) {
        this.stack = this.stack.transmuteCopy(item);
        return this;
    }

    public JSSTElementBuilder setItem(ItemStack stack) {
        this.stack = stack.copy();
        return this;
    }

    public JSSTElementBuilder ui() {
        this.isUIElement = true;
        return this;
    }

    public JSSTElementBuilder cleanText(boolean shouldClean) {
        this.cleanText = shouldClean;
        return this;
    }

    public JSSTElementBuilder setName(@Nullable Component name) {
        this.stack.set(DataComponents.CUSTOM_NAME, this.cleanText ? Component.empty().withStyle(Styles.CLEAN).append(name) : name);
        return this;
    }

    public JSSTElementBuilder modifyName(UnaryOperator<Component> modifier) {
        Component modded = modifier.apply(stack.getHoverName());
        this.stack.set(DataComponents.CUSTOM_NAME, this.cleanText ? Component.empty().withStyle(Styles.CLEAN).append(modded) : modded);
        return this;
    }

    public JSSTElementBuilder setDamage(int damage) {
        this.stack.setDamageValue(damage);
        return this;
    }

    public JSSTElementBuilder setMaxDamage(int maxDamage) {
        this.stack.set(DataComponents.MAX_DAMAGE, maxDamage);
        return this;
    }

    public <T> JSSTElementBuilder setComponent(DataComponentType<T> type, T value) {
        this.stack.set(type, value);
        return this;
    }

    public JSSTElementBuilder removeComponent(DataComponentType<?> componentType) {
        this.stack.remove(componentType);
        return this;
    }

    public JSSTElementBuilder setCount(int count) {
        this.stack.setCount(Mth.clamp(count, 1, 99));
        if (this.stack.getCount() > this.stack.getItem().getDefaultMaxStackSize()) {
            this.stack.set(DataComponents.MAX_STACK_SIZE, count);
        } else {
            this.stack.set(DataComponents.MAX_STACK_SIZE, this.stack.getItem().getDefaultMaxStackSize());
        }
        return this;
    }

    public JSSTElementBuilder setRarity(Rarity rarity) {
        this.stack.set(DataComponents.RARITY, rarity);
        return this;
    }

    public JSSTElementBuilder glow() {
        return glow(true);
    }

    public JSSTElementBuilder glow(@Nullable Boolean shouldGlow) {
        this.stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, shouldGlow);
        return this;
    }

    public JSSTElementBuilder addLoreLineAtStart(Component line) {
        this.stack.update(DataComponents.LORE, ItemLore.EMPTY, this.cleanText ? Component.empty().withStyle(Styles.CLEAN).append(line) : line, (lore, newLine) -> new ItemLore(Util.copyAndAdd(newLine, lore.lines())));
        return this;
    }

    public JSSTElementBuilder addLoreLine(Component line) {
        this.stack.update(DataComponents.LORE, ItemLore.EMPTY, this.cleanText ? Component.empty().withStyle(Styles.CLEAN).append(line) : line, ItemLore::withLineAdded);
        return this;
    }

    // begging for kotlin .also {}
    public JSSTElementBuilder addLoreLine(boolean condition, Component line) {
        if (condition) return addLoreLine(line);
        return this;
    }

    //? if >=1.21.5 {
    public JSSTElementBuilder hideInTooltip(DataComponentType<?> type) {
        this.stack.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, display -> display.withHidden(type, true));
        return this;
    }
    //?}

    public JSSTElementBuilder hideTooltip() {
        //? if <=1.21.4 {
        /*this.stack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        *///?} else
        this.stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
        return this;
    }

    private static <T> void ifNotNull(ItemStack stack, DataComponentType<T> type, UnaryOperator<T> op) {
        stack.update(type, null, comp -> comp != null ? op.apply(comp) : null);
    }

    public JSSTElementBuilder hideDefaultTooltip() {
        //? if <=1.21.4 {
        /*ifNotNull(this.stack, DataComponents.TRIM, comp -> comp.withTooltip(false));
        ifNotNull(this.stack, DataComponents.UNBREAKABLE, comp -> comp.withTooltip(false));
        ifNotNull(this.stack, DataComponents.ENCHANTMENTS, comp -> comp.withTooltip(false));
        ifNotNull(this.stack, DataComponents.STORED_ENCHANTMENTS, comp -> comp.withTooltip(false));
        ifNotNull(this.stack, DataComponents.ATTRIBUTE_MODIFIERS, comp -> comp.withTooltip(false));
        ifNotNull(this.stack, DataComponents.DYED_COLOR, comp -> comp.withTooltip(false));
        ifNotNull(this.stack, DataComponents.CAN_BREAK, comp -> comp.withTooltip(false));
        ifNotNull(this.stack, DataComponents.CAN_PLACE_ON, comp -> comp.withTooltip(false));
        this.stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        *///?} else {
        this.stack.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, display -> {
            for (var comp : this.stack.getComponents()) {
                if (comp.type() != DataComponents.LORE) {
                    display = display.withHidden(comp.type(), true);
                }
            }

            return display;
        });
        //?}
        return this;
    }

    public JSSTElementBuilder leftClick(Component label, Runnable onLeftClick) {
        this.callback = Inputs.leftClick(onLeftClick, this.callback);
        this.stack.update(DataComponents.LORE, ItemLore.EMPTY, Hints.leftClick(label), ItemLore::withLineAdded);
        return this;
    }

    public JSSTElementBuilder rightClick(Component label, Runnable onRightClick) {
        this.callback = Inputs.rightClick(onRightClick, this.callback);
        this.stack.update(DataComponents.LORE, ItemLore.EMPTY, Hints.rightClick(label), ItemLore::withLineAdded);
        return this;
    }

    @Override
    public JSSTElementBuilder setCallback(GuiElementInterface.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    public ItemStack asStack() {
        ItemStack stack = this.stack;

        ItemLore lore = stack.get(DataComponents.LORE);

        // if a ui element, shift any lore lines up 1 to use name as one
        if (this.isUIElement && !this.stack.has(DataComponents.CUSTOM_NAME) && lore != null && !lore.lines().isEmpty()) {
            ItemStack copy = this.stack.copy();
            copy.set(DataComponents.CUSTOM_NAME, lore.lines().getFirst());

            if (lore.lines().size() == 1) {
                copy.remove(DataComponents.LORE);
            } else {
                copy.set(DataComponents.LORE, new ItemLore(lore.lines().subList(1, lore.lines().size())));
            }

            stack = copy;
        }
        return stack.copy();
    }

    @Override
    public GuiElementInterface build() {
        return new GuiElement(this.asStack(), this.callback);
    }
}

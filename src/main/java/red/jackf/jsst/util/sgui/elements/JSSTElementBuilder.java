package red.jackf.jsst.util.sgui.elements;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.mixins.itemeditor.ItemStackAccessor;
import red.jackf.jsst.util.sgui.Hints;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Alternative to {@link eu.pb4.sgui.api.elements.GuiElementBuilder}, optimized for JSST uses
 */
public class JSSTElementBuilder implements GuiElementBuilderInterface<JSSTElementBuilder> {
    private Item item = Items.OAK_PLANKS;
    private int count = 1;
    private @Nullable Component name = null;
    private List<Component> lore = new ArrayList<>();
    private boolean cleanText = true;
    private byte tooltipFlags = 0;
    private @Nullable CompoundTag extraNbt = null;
    private int damage = 0;

    private GuiElementInterface.ClickCallback callback = (a, b, c, d) -> {};
    private boolean glow = false;
    private boolean isUI = false;

    private JSSTElementBuilder() {}

    public static JSSTElementBuilder ui(ItemLike item) {
        return from(item).ui();
    }

    public static JSSTElementBuilder ui(ItemStack stack) {
        return from(stack).ui();
    }

    public static JSSTElementBuilder from(ItemLike item) {
        return from(item.asItem().getDefaultInstance());
    }

    public static JSSTElementBuilder from(ItemStack stack) {
        var builder = new JSSTElementBuilder();
        builder.setItem(stack.getItem());
        builder.setCount(stack.getCount());

        if (!stack.hasTag()) return builder;

        var copy = stack.copy();
        var tag = copy.getTag();
        if (tag != null) {
            if (tag.contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
                var display = tag.getCompound(ItemStack.TAG_DISPLAY);

                if (copy.hasCustomHoverName()) {
                    builder.setName(copy.getHoverName());
                    display.remove(ItemStack.TAG_DISPLAY_NAME);
                }

                if (display.contains(ItemStack.TAG_LORE, Tag.TAG_LIST)) {
                    builder.setLore(display.getList(ItemStack.TAG_LORE, Tag.TAG_STRING).stream()
                            .map(line -> Component.Serializer.fromJson(line.getAsString()))
                            .collect(Collectors.toCollection(ArrayList::new)));
                    display.remove(ItemStack.TAG_LORE);
                }

                if (display.isEmpty()) {
                    tag.remove(ItemStack.TAG_DISPLAY);
                }
            }

            if (copy.isDamaged()) {
                builder.setDamage(copy.getDamageValue());
                tag.remove(ItemStack.TAG_DAMAGE);
            }

            builder.tooltipFlags = (byte) ((ItemStackAccessor) (Object) copy).jsst$itemEditor$getTooltipHideMask();
            tag.remove("HideFlags");

            builder.extraNbt = tag;
        }

        return builder;
    }

    private CompoundTag getOrCreateTag() {
        if (this.extraNbt == null) {
            this.extraNbt = new CompoundTag();
        }

        return this.extraNbt;
    }

    public JSSTElementBuilder ui() {
        this.isUI = true;
        return this;
    }

    public JSSTElementBuilder setItem(ItemLike itemLike) {
        this.item = itemLike.asItem();
        return this;
    }

    public JSSTElementBuilder setName(Component name) {
        this.name = name;
        return this;
    }

    public JSSTElementBuilder setCount(int count) {
        this.count = count;
        return this;
    }

    public JSSTElementBuilder setLore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    public JSSTElementBuilder addLoreLine(Component line) {
        this.lore.add(line);
        return this;
    }

    public JSSTElementBuilder dontCleanText() {
        this.cleanText = false;
        return this;
    }

    public JSSTElementBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public JSSTElementBuilder hideFlags() {
        this.tooltipFlags = (byte) 0b11111111;
        return this;
    }

    public JSSTElementBuilder glow() {
        this.glow = true;
        return this;
    }

    public JSSTElementBuilder setSkullOwner(GameProfile profile, @Nullable MinecraftServer server) {
        if (profile.getId() != null && server != null) {
            if (server.getSessionService().getTextures(profile) == MinecraftProfileTextures.EMPTY) {
                var tmp = server.getSessionService().fetchProfile(profile.getId(), false);
                if (tmp != null) {
                    profile = tmp.profile();
                }
            }

            this.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
        } else {
            this.getOrCreateTag().putString("SkullOwner", profile.getName());
        }
        return this;
    }

    public JSSTElementBuilder setCallback(GuiElement.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    public JSSTElementBuilder leftClick(Component label, Runnable onLeftClick) {
        final GuiElementInterface.ClickCallback oldCallback = this.callback;
        this.callback = (slot, sguiClick, mcClick, gui) -> {
            if (sguiClick == ClickType.MOUSE_LEFT) {
                onLeftClick.run();
            } else {
                oldCallback.click(slot, sguiClick, mcClick, gui);
            }
        };
        if (this.name == null && this.isUI) this.setName(Hints.leftClick(label));
        else this.addLoreLine(Hints.leftClick(label));
        return this;
    }

    public JSSTElementBuilder rightClick(Component label, Runnable onRightClick) {
        final GuiElementInterface.ClickCallback oldCallback = this.callback;
        this.callback = (slot, sguiClick, mcClick, gui) -> {
            if (sguiClick == ClickType.MOUSE_RIGHT) {
                onRightClick.run();
            } else {
                oldCallback.click(slot, sguiClick, mcClick, gui);
            }
        };
        if (this.name == null && this.isUI) this.setName(Hints.rightClick(label));
        else this.addLoreLine(Hints.rightClick(label));
        return this;
    }

    public ItemStack asStack() {
        var stack = new ItemStack(this.item, this.count);

        if (this.extraNbt != null)
            stack.getOrCreateTag().merge(this.extraNbt);

        if (this.name != null)
            stack.setHoverName(cleanText ? Component.empty().withStyle(GuiHelpers.STYLE_CLEARER).append(this.name) : this.name);

        if (this.item.canBeDepleted())
            stack.setDamageValue(this.damage);

        if (!this.lore.isEmpty()) {
            var display = stack.getOrCreateTagElement(ItemStack.TAG_DISPLAY);
            var lore = new ListTag();
            for (Component line : this.lore) {
                if (cleanText) line = Component.empty().withStyle(GuiHelpers.STYLE_CLEARER).append(line);
                lore.add(StringTag.valueOf(Component.Serializer.toJson(line)));
            }
            display.put(ItemStack.TAG_LORE, lore);
        }

        if (this.tooltipFlags != 0) {
            stack.getOrCreateTag().putByte("HideFlags", this.tooltipFlags);
        }

        if (this.glow && EnchantmentHelper.getEnchantments(stack).isEmpty()) {
            stack.enchant(Enchantments.FISHING_LUCK, 1);
            stack.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        }

        return stack;
    }

    public GuiElement build() {
        return new GuiElement(asStack(), this.callback);
    }
}

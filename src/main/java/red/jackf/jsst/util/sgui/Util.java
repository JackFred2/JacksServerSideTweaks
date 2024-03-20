package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.GradientBuilder;
import red.jackf.jsst.mixins.itemeditor.ItemStackAccessor;

import java.util.List;

public interface Util {
    static String snakeToCamelCase(String snakeCase) {
        var builder = new StringBuilder();
        String[] split = snakeCase.split("_");
        for (int i = 0, splitLength = split.length; i < splitLength; i++) {
            var part = split[i];
            if (part.isBlank()) continue;
            if (i != 0) {
                char first = part.charAt(0);
                if (Character.isLowerCase(first)) {
                    builder.append(Character.toUpperCase(first));
                    builder.append(part.substring(1));
                } else {
                    builder.append(part);
                }
            } else {
                builder.append(part);
            }
        }
        return builder.toString();
    }
    static void returnItems(ServerPlayer player, Container container) {
        if (!player.isAlive() || player.hasDisconnected()) {
            for(int i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }
        } else {
            for(int i = 0; i < container.getContainerSize(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
                }
            }
        }
    }

    static Component examples(@Nullable Boolean required, String... options) {
        MutableComponent base = Component.empty().withStyle(Styles.MINOR_LABEL);
        if (required != null) base.append(required ? "(" : "[");
        for (int i = 0; i < options.length; i++) {
            if (i != 0) base.append(Component.literal("|"));
            base.append(Component.literal(options[i]).withStyle(Styles.EXAMPLE));
        }
        if (required != null) base.append(required ? ")" : "]");
        return base;
    }

    static int slot(int column, int row) {
        return row * 9 + column;
    }

    static void clear(SlotHolder holder, int colFrom, int colTo, int rowFrom, int rowTo) {
        fill(holder, ItemStack.EMPTY, colFrom, colTo, rowFrom, rowTo);
    }

    static void fill(SlotHolder holder, ItemStack stack, int colFrom, int colTo, int rowFrom, int rowTo) {
        for (int col = colFrom; col < colTo; col++) {
            for (int row = rowFrom; row < rowTo; row++) {
                holder.setSlot(slot(col, row), stack);
            }
        }
    }

    static void fill(SlotHolder holder, GuiElementInterface stack, int colFrom, int colTo, int rowFrom, int rowTo) {
        for (int col = colFrom; col < colTo; col++) {
            for (int row = rowFrom; row < rowTo; row++) {
                holder.setSlot(slot(col, row), stack);
            }
        }
    }

    static void unhideTooltipPart(ItemStack stack, ItemStack.TooltipPart... parts) {
        int mask = ((ItemStackAccessor) (Object) stack).jsst$itemEditor$getTooltipHideMask();
        for (ItemStack.TooltipPart part : parts) {
            mask &= ~part.getMask();
        }
        if (mask == 0) {
            stack.removeTagKey("HideFlags");
        } else {
            stack.getOrCreateTag().putInt("HideFlags", mask);
        }
    }

    static Component getLabelAsTooltip(ItemStack stack) {
        Style style = Style.EMPTY.withColor(stack.getRarity().color)
                .withItalic(stack.hasCustomHoverName());
        return Component.empty().append(stack.getHoverName()).withStyle(style);
    }

    static Component colourise(Component text, MutableComponent base, Gradient gradient) {
        String str = text.getString();
        final int divisor = str.length() - 1;
        for (int i = 0; i < str.length(); i++) {
            float progress = (float) i / divisor;
            if (progress == 1F) progress = GradientBuilder.END;
            base.append(Component.literal(String.valueOf(str.charAt(i))).withColor(gradient.sample(progress).toARGB()));
        }
        return base;
    }

    static Component formatAsHex(Colour colour) {
        return Component.literal(String.format("#%06X", colour.toARGB() & 0xFFFFFF))
                .setStyle(Styles.MINOR_LABEL);
    }

    static void sendAnvilCost(ServerPlayer player, int containerId, int cost) {
        player.connection.send(new ClientboundContainerSetDataPacket(
                containerId,
                0,
                cost
        ));
    }

    interface Lists {
        static <T> T next(T current, List<T> options) {
            int currentIndex = options.indexOf(current);
            if (currentIndex == -1) throw new IllegalArgumentException("Unknown option");
            if (currentIndex == options.size() - 1) return options.get(0);
            else return options.get(currentIndex + 1);
        }

        static <T> T previous(T current, List<T> options) {
            int currentIndex = options.indexOf(current);
            if (currentIndex == -1) throw new IllegalArgumentException("Unknown option");
            if (currentIndex == 0) return options.get(options.size() - 1);
            else return options.get(currentIndex - 1);
        }

        static <T> void swap(List<T> list, int indexA, int indexB) {
            list.set(indexA, list.set(indexB, list.get(indexA)));
        }
    }

    interface Enums {
        static <E extends Enum<E>> E next(E current) {
            E[] constants = current.getDeclaringClass().getEnumConstants();
            return constants[(current.ordinal() + 1) % constants.length];
        }

        static <E extends Enum<E>> E previous(E current) {
            E[] constants = current.getDeclaringClass().getEnumConstants();
            return constants[current.ordinal() == 0 ? constants.length - 1 : current.ordinal() - 1];
        }
    }

}

package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class DurabilityEditor extends Editor {
    public DurabilityEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.getMaxDamage() > 0;
    }

    @Override
    public ItemStack label() {
        var stack = new ItemStack(Items.GOLDEN_HOE);
        stack.setDamageValue(Items.GOLDEN_HOE.getMaxDamage() / 2);
        return Labels.create(stack).withName("Edit Durability").build();
    }

    private static ItemStack withDamage(ItemStack stack, int damage) {
        var copy = stack.copy();
        copy.setDamageValue(Mth.clamp(damage, 0, stack.getMaxDamage() - 1));
        return copy;
    }

    private static ItemStack withPercentDamage(ItemStack stack, float percent) {
        return withDamage(stack, (int) (stack.getMaxDamage() * percent));
    }

    private void interactOpen() {
        Sounds.interact(player);
        open();
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(Labels.create(stack).keepLore().withHint("Click to finish").build(), this::complete));
        elements.put(25, EditorUtils.reset(() -> {
            Sounds.clear(player);
            this.stack = getOriginal();
            open();
        }));
        elements.put(26, EditorUtils.cancel(this::cancel));

        for (int i = 3; i < 27; i += 9)
            elements.put(i, EditorUtils.divider());

        var tag = stack.getTag();

        var buttons = new ArrayList<ItemGuiElement>();
        if (tag == null || !tag.getBoolean("Unbreakable")) {
            buttons.add(new ItemGuiElement(Labels.create(withDamage(stack, 0)).withName("Full Durability")
                    .build(), () -> {
                stack = withDamage(stack, 0);
                interactOpen();
            }));
            buttons.add(new ItemGuiElement(Labels.create(withPercentDamage(stack, 0.25f)).withName("75% Durability")
                    .build(), () -> {
                stack = withPercentDamage(stack, 0.25f);
                interactOpen();
            }));
            buttons.add(new ItemGuiElement(Labels.create(withPercentDamage(stack, 0.5f)).withName("50% Durability")
                    .build(), () -> {
                stack = withPercentDamage(stack, 0.5f);
                interactOpen();
            }));
            buttons.add(new ItemGuiElement(Labels.create(withPercentDamage(stack, 0.75f)).withName("25% Durability")
                    .build(), () -> {
                stack = withPercentDamage(stack, 0.75f);
                interactOpen();
            }));
            buttons.add(new ItemGuiElement(Labels.create(withPercentDamage(stack, 0.95f)).withName("5% Durability")
                    .build(), () -> {
                stack = withPercentDamage(stack, 0.95f);
                interactOpen();
            }));
            /*buttons.add(new ItemGuiElement(Labels.create(withPercentDamage(stack, 0.99f)).withName("1% Durability")
                    .build(), () -> {
                stack = withPercentDamage(stack, 0.99f);
                interactOpen();
            }));*/
            if (stack.getMaxDamage() > 500)
                buttons.add(new ItemGuiElement(Labels.create(withDamage(stack, stack.getMaxDamage() - 500))
                        .withName("500 Durability").build(), () -> {
                    stack = withDamage(stack, stack.getMaxDamage() - 500);
                    interactOpen();
                }));
            if (stack.getMaxDamage() > 250)
                buttons.add(new ItemGuiElement(Labels.create(withDamage(stack, stack.getMaxDamage() - 250))
                        .withName("250 Durability").build(), () -> {
                    stack = withDamage(stack, stack.getMaxDamage() - 250);
                    interactOpen();
                }));
            if (stack.getMaxDamage() > 100)
                buttons.add(new ItemGuiElement(Labels.create(withDamage(stack, stack.getMaxDamage() - 100))
                        .withName("100 Durability").build(), () -> {
                    stack = withDamage(stack, stack.getMaxDamage() - 100);
                    interactOpen();
                }));
            if (stack.getMaxDamage() > 25)
                buttons.add(new ItemGuiElement(Labels.create(withDamage(stack, stack.getMaxDamage() - 25))
                        .withName("25 Durability").build(), () -> {
                    stack = withDamage(stack, stack.getMaxDamage() - 25);
                    interactOpen();
                }));
            if (stack.getMaxDamage() > 5)
                buttons.add(new ItemGuiElement(Labels.create(withDamage(stack, stack.getMaxDamage() - 5))
                        .withName("5 Durability").build(), () -> {
                    stack = withDamage(stack, stack.getMaxDamage() - 5);
                    interactOpen();
                }));
            buttons.add(new ItemGuiElement(Labels.create(withDamage(stack, stack.getMaxDamage() - 1))
                    .withName("1 Durability").build(), () -> {
                stack = withDamage(stack, stack.getMaxDamage() - 1);
                interactOpen();
            }));
            buttons.add(new ItemGuiElement(Labels.create(Items.STONE_BRICKS).withName("Make Unbreakable").build(), () -> {
                stack.getOrCreateTag().putBoolean("Unbreakable", true);
                interactOpen();
            }));
            buttons.add(new ItemGuiElement(Labels.create(Items.NAME_TAG).withName("Custom Value").withHint("A positive integer or a %").build(), () -> {
                Sounds.interact(player);
                Menus.string(player, String.valueOf(stack.getMaxDamage() - stack.getDamageValue()), s -> {
                    try {
                        if (s.endsWith("%")) {
                            stack = withPercentDamage(stack,1f - Float.parseFloat(s.substring(0, s.length() - 1)) / 100f);
                        } else {
                            stack = withDamage(stack, stack.getMaxDamage() - Integer.parseInt(s));
                        }
                        Sounds.success(player);
                        open();
                    } catch (NumberFormatException ex) {
                        Sounds.error(player);
                        open();
                    }
                });
            }));
        } else {
            buttons.add(new ItemGuiElement(Labels.create(Items.CRACKED_STONE_BRICKS).withName("Remove Unbreakable").build(), () -> {
                var newTag = stack.getOrCreateTag();
                newTag.remove("Unbreakable");
                if (newTag.isEmpty()) stack.setTag(null);
                interactOpen();
            }));
        }


        for (int i = 0; i < buttons.size(); i++) {
            var slot = (i % 5) + (9 * (i / 5)) + 4;
            elements.put(slot, buttons.get(i));
        }

        player.openMenu(EditorUtils.make9x3(Component.literal("Edit Durability"), elements));
    }
}

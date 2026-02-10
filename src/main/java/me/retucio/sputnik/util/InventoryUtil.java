package me.retucio.sputnik.util;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.network.DisconnectEvent;
import me.retucio.sputnik.event.events.interact.OpenScreenEvent;
import me.retucio.sputnik.mixin.accessors.KeyBindingAccessor;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static me.retucio.sputnik.Sputnik.mc;

public class InventoryUtil {

    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int MAIN_INVENTORY_START = 9;
    public static final int MAIN_INVENTORY_END = 35;
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 39;
    public static final int OFFHAND_SLOT = 40;

    private static Inventory echestInv;

    public static ItemStack find(Predicate<ItemStack> predicate) {
        if (mc.player == null) return null;
        for (int i = 0; i <= mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (predicate.test(stack)) {
                return stack;
            }
        }
        return null;
    }

    public static ItemStack getStackOf(Item item) {
        if (mc.player == null) return null;
        for (ItemStack stack : mc.player.getInventory())
            if (stack.getItem() == item)
                return stack;
        return null;
    }

    public static List<ItemStack> getStacksOfTag(TagKey<Item> tag) {
        if (mc.player == null) return null;
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : mc.player.getInventory()) {
            for (TagKey<Item> tagkey : stack.streamTags().toList()) {
                if (tag.equals(tagkey)) {
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    public static List<ItemStack> getStacksOfComponentType(ComponentType<?> componentType) {
        List<ItemStack> stacks = new ArrayList<>();
        if (mc.player == null) return stacks;
        for (ItemStack stack : mc.player.getInventory()) {
            if (stack.getComponents().getTypes().contains(componentType)) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static void swapSlots(int containerSlot1, int containerSlot2, HandledScreen<?> screen) {
        swapSlots(containerSlot1, containerSlot2, screen.getScreenHandler());
    }

    public static void swapSlots(int containerSlot1, int containerSlot2, ScreenHandler handler) {
        if (mc.player == null || mc.interactionManager == null) return;

        int syncId = handler.syncId;

        mc.interactionManager.clickSlot(syncId, containerSlot1, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, containerSlot2, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, containerSlot1, 0, SlotActionType.PICKUP, mc.player);
    }

    public static void swapWithHotbar(int containerSlot, int hotbarSlot) {
        swapWithHotbar(containerSlot, hotbarSlot, mc.player.playerScreenHandler);
    }

    public static void swapWithHotbar(int containerSlot, int hotbarSlot, HandledScreen<?> screen) {
        swapWithHotbar(containerSlot, hotbarSlot, screen.getScreenHandler());
    }

    public static void swapWithHotbar(int containerSlot, int hotbarSlot, ScreenHandler handler) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(handler.syncId, containerSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
    }

    public static void quickMove(int containerSlot, HandledScreen<?> screen) {
        if (mc.player == null || mc.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        mc.interactionManager.clickSlot(handler.syncId, containerSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
    }

    public static void dropItem(int containerSlot, boolean dropStack, HandledScreen<?> screen) {
        if (mc.player == null || mc.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        int button = dropStack ? 1 : 0;
        mc.interactionManager.clickSlot(handler.syncId, containerSlot, button, SlotActionType.THROW, mc.player);
    }

    public static int getContainerSlotByPlayerIndex(ScreenHandler handler, int playerStorageIndex) {
        for (Slot slot : handler.slots)
            if (slot.inventory instanceof PlayerInventory && slot.getIndex() == playerStorageIndex)
                return slot.id;
        return -1;
    }

    public static int getPlayerIndexFromContainerSlot(ScreenHandler handler, int containerSlot) {
        if (containerSlot < 0 || containerSlot >= handler.slots.size()) return -1;
        Slot slot = handler.getSlot(containerSlot);
        if (slot.inventory instanceof PlayerInventory)
            return slot.getIndex();
        return -1;
    }

    public static boolean isPlayerInventorySlot(ScreenHandler handler, int containerSlot) {
        if (containerSlot < 0 || containerSlot >= handler.slots.size()) return false;
        Slot slot = handler.getSlot(containerSlot);
        return slot.inventory instanceof PlayerInventory;
    }

    public static List<Integer> getPlayerContainerSlots(ScreenHandler handler) {
        List<Integer> playerSlots = new ArrayList<>();
        for (Slot slot : handler.slots)
            if (slot.inventory instanceof PlayerInventory)
                playerSlots.add(slot.id);
        return playerSlots;
    }

    public static List<Integer> getSlotsInSameColumn(ScreenHandler handler, int playerStorageIndex) {
        List<Integer> slotsInColumn = new ArrayList<>();
        int column = playerStorageIndex % 9;

        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) {
                int index = slot.getIndex();
                if (index >= 0 && index <= 35) {
                    if (index % 9 == column) {
                        slotsInColumn.add(slot.id);
                    }
                }
            }
        }

        slotsInColumn.sort((a, b) -> {
            int indexA = getPlayerIndexFromContainerSlot(handler, a);
            int indexB = getPlayerIndexFromContainerSlot(handler, b);
            return Integer.compare(indexA / 9, indexB / 9);
        });

        return slotsInColumn;
    }

    public static void moveToOffhand(int containerSlot, HandledScreen<?> screen) {
        if (mc.player == null || mc.interactionManager == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        int offhandContainerSlot = getContainerSlotByPlayerIndex(handler, OFFHAND_SLOT);
        if (offhandContainerSlot == -1) return;

        swapSlots(containerSlot, offhandContainerSlot, screen);
    }

    public static ItemStack getOffhandItem() {
        if (mc.player == null) return ItemStack.EMPTY;
        return mc.player.getOffHandStack();
    }

    public static boolean hasEmptyHotbarSlot() {
        if (mc.player == null) return false;
        PlayerInventory inv = mc.player.getInventory();

        for (int i = HOTBAR_START; i <= HOTBAR_END; i++)
            if (inv.getStack(i).isEmpty())
                return true;
        return false;
    }

    public static int findEmptyHotbarSlot() {
        if (mc.player == null) return -1;
        PlayerInventory inv = mc.player.getInventory();

        for (int i = HOTBAR_START; i <= HOTBAR_END; i++)
            if (inv.getStack(i).isEmpty())
                return i;
        return -1;
    }

    public static List<Integer> findMatchingItems(HandledScreen<?> screen, ItemStack reference) {
        return findMatchingItems(screen.getScreenHandler(), reference);
    }

    public static List<Integer> findMatchingItems(ScreenHandler handler, ItemStack reference) {
        List<Integer> matchingSlots = new ArrayList<>();
        if (reference.isEmpty()) return matchingSlots;

        for (int i = 0; i < handler.slots.size(); i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(reference, stack)) {
                matchingSlots.add(i);
            }
        }

        return matchingSlots;
    }

    public static List<Integer> findItem(Item item) {
        List<Integer> matchingSlots = new ArrayList<>();
        PlayerInventory inv = mc.player.getInventory();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(item)) {
                matchingSlots.add(i);
            }
        }

        return matchingSlots;
    }

    public static void quickMoveAll(HandledScreen<?> screen, ItemStack reference) {
        if (mc.player == null || mc.interactionManager == null) return;

        List<Integer> matchingSlots = findMatchingItems(screen, reference);
        ScreenHandler handler = screen.getScreenHandler();

        for (int slot : matchingSlots) {
            mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
        }
    }

    public static boolean isPlayerScreen(HandledScreen<?> screen) {
        return screen.getScreenHandler() instanceof PlayerScreenHandler;
    }

    public static int getRowOffset(HandledScreen<?> screen) {
        return isPlayerScreen(screen) ? 0 : 9;
    }

    public static int calculateTargetSlot(HandledScreen<?> screen, int currentContainerSlot,
                                          int hotbarColumn, int rowOffset) {
        ScreenHandler handler = screen.getScreenHandler();

        if (!isPlayerScreen(screen)) {
            int targetRow = rowOffset / 9;

            for (Slot slot : handler.slots) {
                if (slot.inventory instanceof PlayerInventory) {
                    int playerIndex = slot.getIndex();

                    if (playerIndex >= 0 && playerIndex <= 35) {
                        int playerColumn = playerIndex % 9;
                        int playerRow = playerIndex / 9;

                        if (playerColumn == hotbarColumn && playerRow == targetRow) {
                            return slot.id;
                        }
                    }
                }
            }
        } else {
            if (currentContainerSlot >= 9 && currentContainerSlot <= 44) {
                int targetPlayerIndex = hotbarColumn + rowOffset;

                if (targetPlayerIndex >= 0 && targetPlayerIndex <= 26) {
                    return targetPlayerIndex + 9;
                } else if (targetPlayerIndex >= 27 && targetPlayerIndex <= 35) {
                    return targetPlayerIndex - 27 + 36;
                }
            }
        }

        return -1;
    }

    public static int getSlotNumberFromKey(int key) {
        for (int i = 0; i < 9; i++)
            if (((KeyBindingAccessor) (mc.options.hotbarKeys[i])).getBoundKey().getCode() == key) return i;
        return -1;
    }

    @SubscribeEvent
    public static void onOpenScreen(OpenScreenEvent event) {
        if (mc.player == null
                || event.getScreen() == null
                || !(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler))
            return;

        if (event.getScreen() instanceof GenericContainerScreen screen
                && screen.getTitle().equals(Text.translatable("container.enderchest")))
            echestInv = handler.getInventory();
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {
        echestInv = null;
    }

    public static Inventory getEchestInv() {
        return echestInv;
    }


}

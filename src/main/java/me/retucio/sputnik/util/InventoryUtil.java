package me.retucio.sputnik.util;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.event.interact.OpenScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class InventoryUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int MAIN_INVENTORY_START = 9;
    public static final int MAIN_INVENTORY_END = 35;
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 39;
    public static final int OFFHAND_SLOT = 40;

    private static Container echestInv;

    public static ItemStack findStack(Predicate<ItemStack> predicate) {
        if (mc.player == null) return null;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (predicate.test(stack)) {
                return stack;
            }
        }
        return null;
    }

    public static int findSlot(Predicate<ItemStack> predicate) {
        if (mc.player == null) return -1;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (predicate.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static List<ItemStack> findAllStacks(Predicate<ItemStack> predicate) {
        List<ItemStack> stacks = new ArrayList<>();
        if (mc.player == null) return stacks;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (predicate.test(stack)) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static List<Integer> findAllSlots(Predicate<ItemStack> predicate) {
        List<Integer> slots = new ArrayList<>();
        if (mc.player == null) return slots;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (predicate.test(stack)) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static void move(int fromSlot, int toSlot) {
        if (mc.player == null || mc.gameMode == null) return;

        int containerId = mc.player.inventoryMenu.containerId;

        mc.gameMode.handleContainerInput(containerId, fromSlot, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(containerId, toSlot, 0, ContainerInput.PICKUP, mc.player);

        if (!mc.player.inventoryMenu.getCarried().isEmpty()) {
            mc.gameMode.handleContainerInput(containerId, fromSlot, 0, ContainerInput.PICKUP, mc.player);
        }
    }

    public static void swapSlots(int containerSlot1, int containerSlot2, AbstractContainerScreen<?> screen) {
        swapSlots(containerSlot1, containerSlot2, screen.getMenu());
    }

    public static void swapSlots(int containerSlot1, int containerSlot2, AbstractContainerMenu handler) {
        if (mc.player == null || mc.gameMode == null) return;

        int containerId = handler.containerId;

        mc.gameMode.handleContainerInput(containerId, containerSlot1, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(containerId, containerSlot2, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(containerId, containerSlot1, 0, ContainerInput.PICKUP, mc.player);
    }

    public static void swapWithHotbar(int containerSlot, int hotbarSlot) {
        if (mc.player == null) return;
        swapWithHotbar(containerSlot, hotbarSlot, mc.player.inventoryMenu);
    }

    public static void swapWithHotbar(int containerSlot, int hotbarSlot, AbstractContainerScreen<?> screen) {
        swapWithHotbar(containerSlot, hotbarSlot, screen.getMenu());
    }

    public static void swapWithHotbar(int containerSlot, int hotbarSlot, AbstractContainerMenu handler) {
        if (mc.player == null || mc.gameMode == null) return;
        mc.gameMode.handleContainerInput(handler.containerId, containerSlot, hotbarSlot, ContainerInput.SWAP, mc.player);
    }

    public static void quickMove(int containerSlot, AbstractContainerScreen<?> screen) {
        if (mc.player == null || mc.gameMode == null) return;

        AbstractContainerMenu handler = screen.getMenu();
        mc.gameMode.handleContainerInput(handler.containerId, containerSlot, 0, ContainerInput.QUICK_MOVE, mc.player);
    }

    public static void dropItem(int containerSlot, boolean dropStack, AbstractContainerScreen<?> screen) {
        if (mc.player == null || mc.gameMode == null) return;

        AbstractContainerMenu handler = screen.getMenu();
        int button = dropStack ? 1 : 0;
        mc.gameMode.handleContainerInput(handler.containerId, containerSlot, button, ContainerInput.THROW, mc.player);
    }

    public static int getContainerSlotByPlayerIndex(AbstractContainerMenu handler, int playerStorageIndex) {
        for (Slot slot : handler.slots)
            if (slot.container instanceof Inventory && slot.getContainerSlot() == playerStorageIndex)
                return slot.index;
        return -1;
    }

    public static int getPlayerIndexFromContainerSlot(AbstractContainerMenu handler, int containerSlot) {
        if (containerSlot < 0 || containerSlot >= handler.slots.size()) return -1;
        Slot slot = handler.getSlot(containerSlot);
        if (slot.container instanceof Inventory)
            return slot.getContainerSlot();
        return -1;
    }

    public static boolean isPlayerInventorySlot(AbstractContainerMenu handler, int containerSlot) {
        if (containerSlot < 0 || containerSlot >= handler.slots.size()) return false;
        Slot slot = handler.getSlot(containerSlot);
        return slot.container instanceof Inventory;
    }

    public static List<Integer> getPlayerContainerSlots(AbstractContainerMenu handler) {
        List<Integer> playerSlots = new ArrayList<>();
        for (Slot slot : handler.slots)
            if (slot.container instanceof Inventory)
                playerSlots.add(slot.index);
        return playerSlots;
    }

    public static List<Integer> getSlotsInSameColumn(AbstractContainerMenu handler, int playerStorageIndex) {
        List<Integer> slotsInColumn = new ArrayList<>();
        int column = playerStorageIndex % 9;

        for (Slot slot : handler.slots) {
            if (slot.container instanceof Inventory) {
                int index = slot.getContainerSlot();
                if (index >= 0 && index <= 35) {
                    if (index % 9 == column) {
                        slotsInColumn.add(slot.index);
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

    public static void moveToOffhand(int containerSlot, AbstractContainerScreen<?> screen) {
        if (mc.player == null || mc.gameMode == null) return;

        AbstractContainerMenu handler = screen.getMenu();
        int offhandContainerSlot = getContainerSlotByPlayerIndex(handler, OFFHAND_SLOT);
        if (offhandContainerSlot == -1) return;

        swapSlots(containerSlot, offhandContainerSlot, screen);
    }

    public static ItemStack getOffhandItem() {
        if (mc.player == null) return ItemStack.EMPTY;
        return mc.player.getOffhandItem();
    }

    public static boolean hasEmptyHotbarSlot() {
        if (mc.player == null) return false;
        Inventory inv = mc.player.getInventory();

        for (int i = HOTBAR_START; i <= HOTBAR_END; i++)
            if (inv.getItem(i).isEmpty())
                return true;
        return false;
    }

    public static int findEmptyHotbarSlot() {
        if (mc.player == null) return -1;
        Inventory inv = mc.player.getInventory();

        for (int i = HOTBAR_START; i <= HOTBAR_END; i++)
            if (inv.getItem(i).isEmpty())
                return i;
        return -1;
    }

    public static List<Integer> findMatchingItems(AbstractContainerScreen<?> screen, ItemStack reference) {
        return findMatchingItems(screen.getMenu(), reference);
    }

    public static List<Integer> findMatchingItems(AbstractContainerMenu handler, ItemStack reference) {
        List<Integer> matchingSlots = new ArrayList<>();
        if (reference.isEmpty()) return matchingSlots;

        for (int i = 0; i < handler.slots.size(); i++) {
            ItemStack stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(reference, stack)) {
                matchingSlots.add(i);
            }
        }

        return matchingSlots;
    }

    public static void quickMoveAll(AbstractContainerScreen<?> screen, ItemStack reference) {
        if (mc.player == null || mc.gameMode == null) return;

        List<Integer> matchingSlots = findMatchingItems(screen, reference);
        AbstractContainerMenu handler = screen.getMenu();

        for (int slot : matchingSlots) {
            mc.gameMode.handleContainerInput(handler.containerId, slot, 0, ContainerInput.QUICK_MOVE, mc.player);
        }
    }

    public static boolean isPlayerScreen(AbstractContainerScreen<?> screen) {
        return screen.getMenu() instanceof InventoryMenu;
    }

    public static int getRowOffset(AbstractContainerScreen<?> screen) {
        return isPlayerScreen(screen) ? 0 : 9;
    }

    public static int calculateTargetSlot(AbstractContainerScreen<?> screen, int currentContainerSlot,
                                          int hotbarColumn, int rowOffset) {
        AbstractContainerMenu handler = screen.getMenu();

        if (!isPlayerScreen(screen)) {
            int targetRow = rowOffset / 9;

            for (Slot slot : handler.slots) {
                if (slot.container instanceof Inventory) {
                    int playerIndex = slot.getContainerSlot();

                    if (playerIndex >= 0 && playerIndex <= 35) {
                        int playerColumn = playerIndex % 9;
                        int playerRow = playerIndex / 9;

                        if (playerColumn == hotbarColumn && playerRow == targetRow) {
                            return slot.index;
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
        for (int i = 0; i < 9; i++) {
            if (KeyUtil.getKey(mc.options.keyHotbarSlots[i]) == key) {
                return i;
            }
        }
        return -1;
    }

    public static boolean hasInHotbar(Item item) {
        if (mc.player == null) return false;
        List<ItemStack> stacks = findAllStacks(stack -> stack.is(item));
        for (ItemStack stack : stacks) {
            int slot = mc.player.getInventory().findSlotMatchingItem(stack);
            if (slot < HOTBAR_END || slot == OFFHAND_SLOT) {
                return true;
            }
        }
        return false;
    }

    @EventListener
    public static void onOpenScreen(OpenScreenEvent event) {
        if (mc.player == null
                || event.getScreen() == null
                || !(mc.player.containerMenu instanceof ChestMenu handler))
            return;

        if (event.getScreen() instanceof ContainerScreen screen
                && screen.getTitle().equals(Component.translatable("container.enderchest")))
            echestInv = handler.getContainer();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        echestInv = null;
    }

    public static Container getEchestInv() {
        return echestInv;
    }
}

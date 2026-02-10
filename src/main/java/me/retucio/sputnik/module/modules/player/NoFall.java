package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import me.retucio.sputnik.util.Lists;
import me.retucio.sputnik.util.NetworkUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;

// todo: arreglar MLGs con bloques
public class NoFall extends Module {

    private boolean placed;

    private final List<Item> mlgItems = List.of(Items.WATER_BUCKET, Items.ENDER_PEARL, Items.POWDER_SNOW_BUCKET,
            Items.SLIME_BLOCK, Items.HAY_BLOCK, Items.HONEY_BLOCK,  Items.BLACK_BED,
            Items.BLUE_BED, Items.BROWN_BED, Items.CYAN_BED, Items.GRAY_BED,
            Items.GREEN_BED, Items.LIGHT_BLUE_BED, Items.LIGHT_GRAY_BED, Items.LIME_BED,
            Items.MAGENTA_BED, Items.ORANGE_BED, Items.PINK_BED, Items.PURPLE_BED,
            Items.RED_BED, Items.WHITE_BED, Items.YELLOW_BED);

    private final EnumSetting<NoFallMode> mode = sgGeneral.add(new EnumSetting<>(
            "modo",
            "de qué manera evitar el daño de caída",
            NoFallMode.class, NoFallMode.PACKET
    ));

    private final ListSetting<Item> items = sgGeneral.add(new ListSetting<>(
            "items",
            "items con los que amortiguar la caída",
            mlgItems,
            Lists.allTrue(mlgItems)
    ));

    private final NumberSetting distance = sgGeneral.add(new NumberSetting(
            "distancia",
            "distancia mínima de caída para aplicar el amortiguamiento",
            3,
            3,
            23,
            1
    ));

    private final BooleanSetting clutch = sgGeneral.add( new BooleanSetting(
            "salvada",
            "cambiar a modo paquete si no se encuentra un cubo disponible",
            true
    ));

    public NoFall() {
        super("tobillos fuertes", "evita el daño de caída", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null
                || mc.world == null
                || mc.getCameraEntity() == null
                || mc.interactionManager == null)
            return;

        if (mc.player.isOnGround()) {
            placed = false;
        }

        if (mode.is(NoFallMode.CLUTCH) && mc.player.fallDistance >= distance.getValue() && !mc.player.isOnGround() && !placed) {
            HitResult result = mc.getCameraEntity().raycast(3, 0, false);
            if (result instanceof BlockHitResult) {
                // encontrar ítem
                ItemStack mlgItem = getMlgItem();
                if (mlgItem == null) {
                    if (!placed) {
                        ChatUtil.warn("amortiguador (agua, etc.) no encontrado");
                        placed = true;
                        if (clutch.getValue()) {
                            mode.setValue(NoFallMode.PACKET);
                            ChatUtil.info("cambiado a modo de paquetes");
                        }
                    }
                    return;
                }

                // mirar abajo
                mc.player.setPitch(180);

                int slot = mc.player.getInventory().getSlotWithStack(mlgItem);
                mc.player.getInventory().setSelectedSlot(slot);

                // usar ítem
                ActionResult didPlace = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (didPlace.isAccepted())
                    placed = true;
            }
        }
    }

    @SubscribeEvent
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null
                || mc.getNetworkHandler() == null
                || !mode.is(NoFallMode.PACKET)
                || mc.player.getAbilities().creativeMode
                || mc.player.fallDistance < distance.getValue())
            return;

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (!packet.isOnGround()) {
                if (mc.player.fallDistance >= 3) {
                    event.cancel();
                    sendOnGroundPacket();
                }
            }
        }
    }

    private void sendOnGroundPacket() {
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ(),
                mc.player.getYaw(),
                mc.player.getPitch(),
                true,
                mc.player.horizontalCollision
        ));
    }

    private ItemStack getMlgItem() {
        for (Item item : items.getEnabledOptions()) {
            ItemStack stack = InventoryUtil.getStackOf(item);
            if (stack != null && PlayerInventory.isValidHotbarIndex(mc.player.getInventory().getSlotWithStack(stack))) {
                return stack;
            }
        }
        return null;
    }

    private enum NoFallMode {
        CLUTCH("waterdrop"),
        PACKET("paquetes");

        private final String name;
        NoFallMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}

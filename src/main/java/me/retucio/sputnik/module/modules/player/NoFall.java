package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

// todo: implementar otros tipos de MLGs (cama, miel, slime, heno, ...)
public class NoFall extends Module {

    private boolean placed;

    private final EnumSetting<NoFallMode> mode = sgGeneral.add(new EnumSetting<>(
            "modo",
            "de qué manera evitar el daño de caída",
            NoFallMode.class, NoFallMode.PACKET
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

        if (mode.is(NoFallMode.CLUTCH) && mc.player.fallDistance >= 3 && !mc.player.isOnGround() && !placed) {
            HitResult result = mc.getCameraEntity().raycast(3, 0, false);
            if (result instanceof BlockHitResult) {
                // encontrar cubo
                ItemStack waterBucket = InventoryUtil.getStackOfItem(Items.WATER_BUCKET);
                if (waterBucket == null) {
                    ChatUtil.warn("cubo de agua no encontrado");
                    if (clutch.getValue()) {
                        mode.setValue(NoFallMode.PACKET);
                        ChatUtil.info("cambiado a modo de paquetes");
                    }
                    return;
                }

                // mirar abajo
                mc.player.setPitch(180);

                int slot = mc.player.getInventory().getSlotWithStack(waterBucket);
                mc.player.getInventory().setSelectedSlot(slot);

                // colocar agua
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
                || mc.player.getAbilities().creativeMode)
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

    private enum NoFallMode {
        CLUTCH("waterdrop"),
        PACKET("paquetes");

        private final String name;
        NoFallMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}

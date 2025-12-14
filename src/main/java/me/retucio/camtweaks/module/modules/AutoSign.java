package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.OpenScreenEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import me.retucio.camtweaks.mixin.accessor.AbstractSignEditScreenAccessor;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.math.BlockPos;

public class AutoSign extends Module {

    public String[] text = null;

    public AutoSign() {
        super("autocartel", "te permite colocar carteles con el mismo texto sin tener que escribirlo uno por uno");
    }

    @Override
    public void onEnable() {
        if (text == null) ChatUtil.info("coloca un primer cartel, del cual copiar el contenido");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        text = null;
        super.onDisable();
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        if (event.getScreen() instanceof AbstractSignEditScreen screen && text != null && mc != null && mc.player != null) {
            event.cancel();
            SignBlockEntity sign = ((AbstractSignEditScreenAccessor) screen).getBlockEntity();
            fillText(sign.getPos(), sign.isPlayerFacingFront(mc.player), text);
        }
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof UpdateSignC2SPacket packet) {
            if (text == null) text = packet.getText().clone();
            else System.arraycopy(text, 0, packet.getText(), 0, 4);
        }
    }

    private void fillText(BlockPos blockPos, boolean front, String[] text) {
        mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(
            blockPos, front, text[0], text[1], text[2], text[3]
        ));
    }
}
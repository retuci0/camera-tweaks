package me.retucio.sputnik.module.modules.world;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.interact.OpenScreenEvent;
import me.retucio.sputnik.event.events.network.PacketEvent;
import me.retucio.sputnik.mixin.accessors.AbstractSignEditScreenAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.math.BlockPos;

public class AutoSign extends Module {

    private String[] text = null;

    public AutoSign() {
        super("autocartel",
                "te permite colocar carteles con el mismo texto sin tener que escribirlo uno por uno",
                Category.WORLD);
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
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.getScreen() instanceof AbstractSignEditScreen screen && text != null && mc != null && mc.player != null) {
            event.cancel();
            SignBlockEntity sign = ((AbstractSignEditScreenAccessor) screen).getBlockEntity();
            fillText(sign.getPos(), sign.isPlayerFacingFront(mc.player), text);
        }
    }

    @SubscribeEvent
    private void onSendPacket(PacketEvent.Send event) {
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
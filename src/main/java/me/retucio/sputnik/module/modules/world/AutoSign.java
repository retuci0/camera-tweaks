package me.retucio.sputnik.module.modules.world;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.interact.OpenScreenEvent;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.mixin.accessors.AbstractSignEditScreenAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;


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

    @EventListener
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.getScreen() instanceof AbstractSignEditScreen screen && text != null && mc != null && mc.player != null) {
            event.cancel();
            SignBlockEntity sign = ((AbstractSignEditScreenAccessor) screen).getBlockEntity();
            fillText(sign.getBlockPos(), sign.isFacingFrontText(mc.player), text);
        }
    }

    @EventListener
    private void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundSignUpdatePacket packet) {
            if (text == null) text = packet.getLines().clone();
            else System.arraycopy(text, 0, packet.getLines(), 0, 4);
        }
    }

    private void fillText(BlockPos blockPos, boolean front, String[] text) {
        mc.getConnection().send(new ServerboundSignUpdatePacket(
            blockPos, front, text[0], text[1], text[2], text[3]
        ));
    }
}
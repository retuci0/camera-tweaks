package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.AttackEntityEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;

public class AttributeSwapper extends Module {

    public NumberSetting slot = addSetting(new NumberSetting("slot", "slot al que cambiar", 1, 1, 9, 1));
    public BooleanSetting swap = addSetting(new BooleanSetting("cambiar de vuelta", "volver al slot inicial tras haber aplicado el swapping", true));
    public NumberSetting swapDelay = addSetting(new NumberSetting("delay de cambio de vuelta", "tiempo que se tarda en volver al slot inicial", 1, 1, 20, 1));

    private int delay = 0;
    private int prevSlot = -1;

    public AttributeSwapper() {
        super("attribute-swapper", "utiliza el \"attribute swapping\" entre dos herramientas para negar daño de durabilidad o aplicar encantamientos adicionales a estas");
    }

    @Override
    public void onTick() {
        if (mc.player == null || !swap.isEnabled()) return;
        if (delay < swapDelay.getIntValue()) delay++;
        if (delay >= swapDelay.getIntValue() && prevSlot != -1) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
            prevSlot = -1;
            delay = 0;
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        prevSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot.getIntValue() - 1);
        delay = 0;
    }
}

package me.retucio.sputnik.module.modules.combat;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.interact.AttackEntityEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;

public class AttributeSwapper extends Module {

    private final NumberSetting slot = sgGeneral.add(new NumberSetting("slot", "slot al que cambiar", 1, 1, 9, 1));
    private final BooleanSetting swap = sgGeneral.add(new BooleanSetting("cambiar de vuelta", "volver al slot inicial tras haber aplicado el swapping", true));
    private final NumberSetting swapDelay = sgGeneral.add(new NumberSetting("delay de cambio de vuelta", "tiempo que se tarda en volver al slot inicial", 1, 1, 20, 1));

    private int delay = 0;
    private int prevSlot = -1;

    public AttributeSwapper() {
        super("attribute-swapper",
                "utiliza el \"attribute swapping\" entre dos herramientas para negar daño de durabilidad o aplicar encantamientos adicionales a estas",
                Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || !swap.getValue()) return;
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

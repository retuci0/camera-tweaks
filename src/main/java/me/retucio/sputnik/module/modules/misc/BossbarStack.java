package me.retucio.sputnik.module.modules.misc;

import me.retucio.sputnik.event.events.RenderBossbarEvent;
import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class BossbarStack extends Module {

    public BooleanSetting stackBars = sgGeneral.add(new BooleanSetting(
            "apilar barras", "apilar las bossbars",
            true
    ));

    public BooleanSetting hideNames = sgGeneral.add(new BooleanSetting(
            "esconder nombres", "no renderiza el nombre de los bosses",
            false
    ));

    public NumberSetting spaceReduction = sgGeneral.add(new NumberSetting(
            "reducción de espacio",
            "cuánto reducir el espacio entre bossbars",
            0,
            0,
            10,
            0.1
    ));


    private final Map<ClientBossBar, Integer> bossBarMap = new WeakHashMap<>();

    public BossbarStack() {
        super("apilar bossbars",
                "apila bossbars para reducir el espacio que ocupan en pantalla",
                Category.MISC);
    }

    @SubscribeEvent
    private void onRenderBossText(RenderBossbarEvent.BossText event) {
        if (hideNames.getValue()) {
            event.setName(Text.empty());
            return;
        } else if (bossBarMap.isEmpty() || !stackBars.getValue()) return;

        ClientBossBar bar = event.getBossBar();
        Integer amount = bossBarMap.get(bar);
        bossBarMap.remove(bar);

        if (amount != null && !hideNames.getValue())
            event.setName(event.getName().copy().append(" x" + amount));
    }

    @SubscribeEvent
    private void onRenderBossSpacing(RenderBossbarEvent.BossSpacing event) {
        event.setSpacing(10 - spaceReduction.getIntValue());
    }

    @SubscribeEvent
    private void onRenderBossBars(RenderBossbarEvent.BossIterator event) {
        if (stackBars.getValue()) {
            HashMap<String, ClientBossBar> chosenBarMap = new HashMap<>();
            event.getIterator().forEachRemaining(bar -> {
                String name = bar.getName().getString();
                if (chosenBarMap.containsKey(name))
                    bossBarMap.compute(chosenBarMap.get(name), (clientBossBar, integer) -> (integer == null) ? 2 : integer + 1);
                else
                    chosenBarMap.put(name, bar);
            });
            event.setIterator(chosenBarMap.values().iterator());
        }
    }
}
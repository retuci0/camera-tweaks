package me.retucio.camtweaks.module.modules.misc;

import me.retucio.camtweaks.event.events.RenderBossbarEvent;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class BossbarStack extends Module {

    public BooleanSetting stackBars = addSetting(new BooleanSetting("apilar barras", "apilar las bossbars", true));
    public BooleanSetting hideNames = addSetting(new BooleanSetting("esconder nombres", "no renderiza el nombre de los bosses", false));
    public NumberSetting spaceReduction = addSetting(new NumberSetting("reducción de espacio", "cuánto reducir el espacio entre bossbars",
            0, 0, 10, 0.1));

    private final Map<ClientBossBar, Integer> bossBarMap = new WeakHashMap<>();

    public BossbarStack() {
        super("apilar bossbars",
                "apila bossbars para reducir el espacio que ocupan en pantalla",
                Category.MISC);
    }

    @SubscribeEvent
    private void onRenderBossText(RenderBossbarEvent.BossText event) {
        if (hideNames.isEnabled()) {
            event.setName(Text.empty());
            return;
        } else if (bossBarMap.isEmpty() || !stackBars.isEnabled()) return;

        ClientBossBar bar = event.getBossBar();
        Integer amount = bossBarMap.get(bar);
        bossBarMap.remove(bar);

        if (amount != null && !hideNames.isEnabled())
            event.setName(event.getName().copy().append(" x" + amount));
    }

    @SubscribeEvent
    private void onRenderBossSpacing(RenderBossbarEvent.BossSpacing event) {
        event.setSpacing(10 - spaceReduction.getIntValue());
    }

    @SubscribeEvent
    private void onRenderBossBars(RenderBossbarEvent.BossIterator event) {
        if (stackBars.isEnabled()) {
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
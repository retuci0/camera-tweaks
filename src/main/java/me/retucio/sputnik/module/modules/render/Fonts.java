package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.OptionSetting;
import me.retucio.sputnik.util.Lists;


public class Fonts extends Module {

    private final OptionSetting<String> font = sgGeneral.add(new OptionSetting<>(
            "fuente", "fuente a emplear",
            Lists.fontList, "ubuntu")
    );

    private final BooleanSetting reload = sgGeneral.add(new BooleanSetting(
       "recargar", "recargar automáticamente",
       false
    ));

    public Fonts() {
        super("fuentes",
                "modifica el tipo de letra utilizado al renderizar texto",
                Category.RENDER);

        font.onUpdate(v -> reload());
    }

    @Override
    public void onEnable() {
        reload();
    }

    @Override
    public void onDisable() {
        reload();
    }

    private void reload() {
        if (mc.getResourceManager() != null && reload.getValue()) {
            mc.reloadResourcePacks();
        }
    }

    public String getFont() {
        return font.getValue();
    }
}

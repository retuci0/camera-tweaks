package me.retucio.camtweaks.module.modules.player;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.ClientPlayerEntityMixin
 */

public class PortalGUI extends Module {

    public PortalGUI() {
        super("interfaz en portales",
                "te permite abrir interfaces dentro de portales, como el chat o el inventario",
                Category.PLAYER);
    }
}
package me.retucio.sputnik.module.modules.inventory;

import me.retucio.sputnik.mixin.mixins.player.LocalPlayerMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;

/** continúa en:
 * @see LocalPlayerMixin
 */

public class PortalGUI extends Module {

    public PortalGUI() {
        super("interfaz en portales",
                "te permite abrir interfaces dentro de portales, como el chat o el inventario",
                Category.INVENTORY);
    }
}
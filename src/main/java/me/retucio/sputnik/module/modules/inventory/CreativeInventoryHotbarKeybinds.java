package me.retucio.sputnik.module.modules.inventory;

import me.retucio.sputnik.mixin.mixins.screen.CreativeInventoryScreenMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;


/** continúa en
 * @see CreativeInventoryScreenMixin
 *
 * @author retucio
 */

public class CreativeInventoryHotbarKeybinds extends Module {

    public CreativeInventoryHotbarKeybinds() {
        super("hotkeys inv. creativo",
                "te permite seguir usando teclas de la hotbar para mover items en la pestaña de búsqueda del creativo en vez de escribir en la barra",
                Category.INVENTORY);
    }
}

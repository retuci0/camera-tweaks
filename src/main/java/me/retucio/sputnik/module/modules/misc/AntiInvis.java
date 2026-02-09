package me.retucio.sputnik.module.modules.misc;

import me.retucio.sputnik.mixin.mixins.entity.EntityMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;

/** continúa en:
 * @see EntityMixin
 */

public class AntiInvis extends Module {

    public AntiInvis() {
        super("anti invis",
                "te permite ver a los jugadores invisibles",
                Category.MISC);
    }
}

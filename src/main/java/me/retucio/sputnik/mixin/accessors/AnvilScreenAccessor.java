package me.retucio.sputnik.mixin.accessors;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilScreen.class)
public interface AnvilScreenAccessor {

    @Accessor("name")
    EditBox getNameField();

    @Accessor("name")
    void setNameField(EditBox nameField);
}
